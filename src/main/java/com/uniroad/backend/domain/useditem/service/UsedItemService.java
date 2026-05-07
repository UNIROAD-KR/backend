package com.uniroad.backend.domain.useditem.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.useditem.dto.UsedItemRequestDto;
import com.uniroad.backend.domain.useditem.dto.UsedItemResponseDto;
import com.uniroad.backend.domain.useditem.entity.UsedItem;
import com.uniroad.backend.domain.useditem.entity.UsedItemImage;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsedItemService {

    private final UsedItemRepository usedItemRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createUsedItem(UsedItemRequestDto requestDto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UsedItem usedItem = UsedItem.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .region(requestDto.getRegion())
                .semester(requestDto.getSemester())
                .author(author)
                .build();

        if (requestDto.getImageUrls() != null) {
            for (String url : requestDto.getImageUrls()) {
                UsedItemImage image = UsedItemImage.builder()
                        .imageUrl(url)
                        .build();
                usedItem.addImage(image);
            }
        }

        return usedItemRepository.save(usedItem).getId();
    }

    public List<UsedItemResponseDto> getUsedItems() {
        String userRegion = null;
        try {
            Long memberId = SecurityUtil.getCurrentMemberId();
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                userRegion = member.getDispatchedRegion();
            }
        } catch (Exception e) {
            // 비로그인 사용자의 경우 정렬 기준에서 지역 일치 항목이 뒤로 밀리거나 기본 정렬 적용
        }

        List<UsedItem> items;
        if (userRegion != null) {
            items = usedItemRepository.findAllSortedByRegion(userRegion);
        } else {
            items = usedItemRepository.findAllByOrderByCreatedAtDesc();
        }

        return items.stream()
                .map(UsedItemResponseDto::from)
                .collect(Collectors.toList());
    }

    public UsedItemResponseDto getUsedItem(Long id) {
        UsedItem usedItem = usedItemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USED_ITEM_NOT_FOUND));
        return UsedItemResponseDto.from(usedItem);
    }

    @Transactional
    public void deleteUsedItem(Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UsedItem usedItem = usedItemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USED_ITEM_NOT_FOUND));

        // 작성자 본인이거나 관리자인 경우에만 삭제 가능
        if (!usedItem.getAuthor().getId().equals(memberId) && !member.getRole().equals(com.uniroad.backend.domain.member.entity.Role.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        usedItemRepository.delete(usedItem);
    }
}
