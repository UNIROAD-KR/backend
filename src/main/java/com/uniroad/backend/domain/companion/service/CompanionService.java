package com.uniroad.backend.domain.companion.service;

import com.uniroad.backend.domain.companion.dto.CompanionPostRequest;
import com.uniroad.backend.domain.companion.dto.CompanionPostResponse;
import com.uniroad.backend.domain.companion.entity.CompanionPost;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanionService {

    private final CompanionPostRepository companionPostRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createPost(Long memberId, CompanionPostRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        CompanionPost post = CompanionPost.builder()
                .member(member)
                .title(request.title())
                .content(request.content())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .country(request.country())
                .region(request.region())
                .chatLink(request.chatLink())
                .status(request.status())
                .capacity(request.capacity())
                .genderRatio(request.genderRatio())
                .build();

        return companionPostRepository.save(post).getId();
    }

    @Transactional
    public void updatePost(Long memberId, Long postId, CompanionPostRequest request) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        post.update(
                request.title(),
                request.content(),
                request.startDate(),
                request.endDate(),
                request.country(),
                request.region(),
                request.chatLink(),
                request.status(),
                request.capacity(),
                request.genderRatio()
        );
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        companionPostRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<CompanionPostResponse> getPostsByMemberCountry(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String country = member.getDispatchedCountry();
        if (country == null) {
            // 국가 정보가 없는 경우 빈 리스트 반환 또는 전체 조회 (사용자 의도에 따라 다름)
            // 여기선 빈 리스트를 반환하거나 예외를 던지는 대신 전체 조회를 할 수도 있지만
            // "나라기반으로"라고 했으므로 국가 정보가 필수라고 가정.
            return List.of();
        }

        return companionPostRepository.findAllByCountryOrderByCreatedAtDesc(country)
                .stream()
                .map(CompanionPostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanionPostResponse> getMyPosts(Long memberId) {
        return companionPostRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(CompanionPostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanionPostResponse getPostDetail(Long postId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return CompanionPostResponse.from(post);
    }
}
