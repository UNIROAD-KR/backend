package com.uniroad.backend.domain.companion.service;

import com.uniroad.backend.domain.companion.dto.CompanionPostRequest;
import com.uniroad.backend.domain.companion.dto.CompanionPostResponse;
import com.uniroad.backend.domain.companion.entity.CompanionPost;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
                .currentParticipants(request.currentParticipants())
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
                request.currentParticipants(),
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
    public CursorPageResponse<CompanionPostResponse> getPosts(Long cursorId, int size) {
        int requestSize = normalizeSize(size);
        List<CompanionPost> posts = companionPostRepository.findByCursor(
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        boolean hasNext = posts.size() > requestSize;
        List<CompanionPost> pagePosts = hasNext ? posts.subList(0, requestSize) : posts;
        List<CompanionPostResponse> items = pagePosts.stream()
                .map(CompanionPostResponse::from)
                .collect(Collectors.toList());

        Long nextCursorId = hasNext ? pagePosts.get(pagePosts.size() - 1).getId() : null;
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
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

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
