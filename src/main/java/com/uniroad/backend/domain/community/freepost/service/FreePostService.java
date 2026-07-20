package com.uniroad.backend.domain.community.freepost.service;

import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostDetailResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostLikeResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostSearchRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostSummaryResponse;
import com.uniroad.backend.domain.community.freepost.entity.FreePost;
import com.uniroad.backend.domain.community.freepost.entity.FreePostComment;
import com.uniroad.backend.domain.community.freepost.entity.FreePostLike;
import com.uniroad.backend.domain.community.freepost.repository.FreePostCommentRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostLikeRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import com.uniroad.backend.domain.scrap.repository.ScrapRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreePostService {

    private final FreePostRepository freePostRepository;
    private final FreePostCommentRepository freePostCommentRepository;
    private final FreePostLikeRepository freePostLikeRepository;
    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;

    public CursorPageResponse<FreePostSummaryResponse> getPosts(Long cursorId, String keyword, int size) {
        int requestSize = normalizeSize(size);
        List<FreePost> posts = freePostRepository.findByCursorAndKeyword(
                cursorId,
                normalizeKeyword(keyword),
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<FreePostSummaryResponse> searchPosts(Long cursorId, int size, FreePostSearchRequest request) {
        int requestSize = normalizeSize(size);
        List<FreePost> posts = freePostRepository.searchByCursor(
                cursorId,
                normalizeText(request.title()),
                normalizeText(request.content()),
                PageRequest.of(0, requestSize + 1)
        );
        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<FreePostSummaryResponse> getMyPosts(Long memberId, Long cursorId, int size) {
        int requestSize = normalizeSize(size);
        List<FreePost> posts = freePostRepository.findByMemberIdAndCursor(
                memberId,
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<FreePostSummaryResponse> getMyLikedPosts(Long memberId, Long cursorId, int size) {
        int requestSize = normalizeSize(size);
        List<FreePost> posts = freePostRepository.findLikedByMemberIdAndCursor(
                memberId,
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public CursorPageResponse<FreePostSummaryResponse> getMyScrappedPosts(Long memberId, Long cursorId, int size) {
        int requestSize = normalizeSize(size);
        List<FreePost> posts = freePostRepository.findScrappedByMemberIdAndCursor(
                memberId,
                ScrapTargetType.FREE_POST,
                cursorId,
                PageRequest.of(0, requestSize + 1)
        );

        return toCursorResponse(posts, requestSize);
    }

    public List<FreePostSummaryResponse> getTopLikedPosts() {
        return freePostRepository.findTopByLikeCount(PageRequest.of(0, 3))
                .stream()
                .map(post -> FreePostSummaryResponse.from(
                        post,
                        freePostLikeRepository.countByFreePostId(post.getId()),
                        scrapRepository.countByTargetTypeAndTargetId(ScrapTargetType.FREE_POST, post.getId()),
                        freePostCommentRepository.countByFreePostId(post.getId())
                ))
                .toList();
    }

    private CursorPageResponse<FreePostSummaryResponse> toCursorResponse(List<FreePost> posts, int requestSize) {
        boolean hasNext = posts.size() > requestSize;
        List<FreePost> pagePosts = hasNext ? posts.subList(0, requestSize) : posts;
        List<FreePostSummaryResponse> items = pagePosts.stream()
                .map(post -> FreePostSummaryResponse.from(
                        post,
                        freePostLikeRepository.countByFreePostId(post.getId()),
                        scrapRepository.countByTargetTypeAndTargetId(ScrapTargetType.FREE_POST, post.getId()),
                        freePostCommentRepository.countByFreePostId(post.getId())
                ))
                .toList();

        Long nextCursorId = hasNext ? pagePosts.get(pagePosts.size() - 1).getId() : null;
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }

    public FreePostDetailResponse getPost(Long memberId, Long postId) {
        FreePost post = getPostEntity(postId);
        List<FreePostCommentResponse> comments = freePostCommentRepository.findByFreePostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(comment -> FreePostCommentResponse.from(comment, memberId))
                .toList();

        return FreePostDetailResponse.from(
                post,
                freePostLikeRepository.countByFreePostId(postId),
                scrapRepository.countByTargetTypeAndTargetId(ScrapTargetType.FREE_POST, postId),
                comments.size(),
                freePostLikeRepository.existsByFreePostIdAndMemberId(postId, memberId),
                memberId,
                comments
        );
    }

    @Transactional
    public Long createPost(Long memberId, FreePostRequest request) {
        Member member = getMember(memberId);
        FreePost post = FreePost.builder()
                .member(member)
                .title(request.title().trim())
                .content(request.content().trim())
                .country(request.country().trim())
                .status(request.status().trim())
                .imageUrls(normalizeImageUrls(request.imageUrls()))
                .build();

        return freePostRepository.save(post).getId();
    }

    @Transactional
    public void updatePost(Long memberId, Long postId, FreePostRequest request) {
        FreePost post = getPostEntity(postId);
        validateOwner(post.getMember().getId(), memberId);

        post.update(
                request.title().trim(),
                request.content().trim(),
                request.country().trim(),
                request.status().trim(),
                normalizeImageUrls(request.imageUrls())
        );
    }

    @Transactional
    public void deletePost(Long memberId, Long postId) {
        FreePost post = getPostEntity(postId);
        validateOwner(post.getMember().getId(), memberId);
        freePostCommentRepository.deleteAllByFreePostId(postId);
        freePostLikeRepository.deleteAllByFreePostId(postId);
        scrapRepository.deleteAllByTargetTypeAndTargetId(ScrapTargetType.FREE_POST, postId);
        freePostRepository.delete(post);
    }

    @Transactional
    public FreePostLikeResponse toggleLike(Long memberId, Long postId) {
        FreePost post = getPostEntity(postId);
        Member member = getMember(memberId);

        return freePostLikeRepository.findByFreePostIdAndMemberId(postId, memberId)
                .map(like -> {
                    freePostLikeRepository.delete(like);
                    return new FreePostLikeResponse(false, freePostLikeRepository.countByFreePostId(postId));
                })
                .orElseGet(() -> {
                    freePostLikeRepository.save(FreePostLike.builder()
                            .freePost(post)
                            .member(member)
                            .build());
                    return new FreePostLikeResponse(true, freePostLikeRepository.countByFreePostId(postId));
                });
    }

    @Transactional
    public FreePostCommentResponse createComment(Long memberId, Long postId, FreePostCommentRequest request) {
        FreePost post = getPostEntity(postId);
        Member member = getMember(memberId);

        FreePostComment comment = freePostCommentRepository.save(FreePostComment.builder()
                .freePost(post)
                .member(member)
                .content(request.content().trim())
                .build());

        return FreePostCommentResponse.from(comment, memberId);
    }

    @Transactional
    public void deleteComment(Long memberId, Long postId, Long commentId) {
        FreePostComment comment = freePostCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!comment.getFreePost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        validateOwner(comment.getMember().getId(), memberId);

        freePostCommentRepository.delete(comment);
    }

    @Transactional
    public FreePostCommentResponse updateComment(Long memberId, Long postId, Long commentId, FreePostCommentRequest request) {
        FreePostComment comment = freePostCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!comment.getFreePost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        validateOwner(comment.getMember().getId(), memberId);

        comment.updateContent(request.content().trim());
        return FreePostCommentResponse.from(comment, memberId);
    }

    private FreePost getPostEntity(Long postId) {
        return freePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateOwner(Long ownerId, Long memberId) {
        if (!ownerId.equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return new ArrayList<>();
        }
        return imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
