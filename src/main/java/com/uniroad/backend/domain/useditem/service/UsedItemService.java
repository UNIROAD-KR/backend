package com.uniroad.backend.domain.useditem.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.useditem.dto.*;
import com.uniroad.backend.domain.useditem.entity.TradeCategoryImage;
import com.uniroad.backend.domain.useditem.entity.TradeItem;
import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        UsedItemPost usedItemPost = UsedItemPost.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .region(requestDto.getRegion())
                .semester(requestDto.getSemester())
                .country(requestDto.getCountry())
                .thumbnailImageUrl(requestDto.getThumbnailImageUrl())
                .author(author)
                .build();

        // 카테고리 이미지 저장
        if (requestDto.getCategoryImages() != null) {

            for (TradeCategoryImageRequestDto imageDto : requestDto.getCategoryImages()) {

                TradeCategoryImage image = TradeCategoryImage.builder()
                        .category(imageDto.getCategory())
                        .imageUrl(imageDto.getImageUrl())
                        .build();

                usedItemPost.addImage(image);
            }
        }

        // 아이템 저장
        if (requestDto.getItems() != null) {

            for (TradeItemRequestDto itemDto : requestDto.getItems()) {

                TradeItem item = TradeItem.builder()
                        .category(itemDto.getCategory())
                        .name(itemDto.getName())
                        .quantity(itemDto.getQuantity())
                        .build();

                usedItemPost.addItem(item);
            }
        }

        return usedItemRepository.save(usedItemPost).getId();
    }

    public UsedItemResponseDto getUsedItem(Long id) {
        UsedItemPost usedItemPost = usedItemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USED_ITEM_NOT_FOUND));
        return UsedItemResponseDto.from(usedItemPost);
    }

    @Transactional
    public void deleteUsedItem(Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        UsedItemPost usedItemPost = usedItemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USED_ITEM_NOT_FOUND));

        // 작성자 본인이거나 관리자인 경우에만 삭제 가능
        if (!usedItemPost.getAuthor().getId().equals(memberId) && !member.getRole().equals(com.uniroad.backend.domain.member.entity.Role.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        usedItemRepository.delete(usedItemPost);
    }

    public CursorPageResponse<UsedItemSummaryResponseDto> getUsedItems(Long cursorId, int size) {
        int requestSize = normalizeSize(size);
        List<UsedItemPost> posts = usedItemRepository.findByCursor(
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<UsedItemSummaryResponseDto> getMyUsedItems(Long cursorId, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        int requestSize = normalizeSize(size);
        List<UsedItemPost> posts = usedItemRepository.findByAuthorIdAndCursor(
                memberId,
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    private CursorPageResponse<UsedItemSummaryResponseDto> toCursorResponse(List<UsedItemPost> posts, int requestSize) {
        boolean hasNext = posts.size() > requestSize;
        List<UsedItemPost> pagePosts = hasNext ? posts.subList(0, requestSize) : posts;
        List<UsedItemSummaryResponseDto> items = pagePosts.stream()
                .map(UsedItemSummaryResponseDto::from)
                .toList();

        Long nextCursorId = hasNext ? pagePosts.get(pagePosts.size() - 1).getId() : null;
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
