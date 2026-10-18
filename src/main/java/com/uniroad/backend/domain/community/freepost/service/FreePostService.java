package com.uniroad.backend.domain.community.freepost.service;

import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostDetailResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostLikeResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostSearchRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostSort;
import com.uniroad.backend.domain.community.freepost.dto.FreePostSummaryResponse;
import com.uniroad.backend.domain.community.freepost.entity.FreePost;
import com.uniroad.backend.domain.community.freepost.entity.FreePostCategory;
import com.uniroad.backend.domain.community.freepost.entity.FreePostComment;
import com.uniroad.backend.domain.community.freepost.entity.FreePostLike;
import com.uniroad.backend.domain.community.freepost.repository.FreePostCommentRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostLikeRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.notification.service.NotificationService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreePostService {

    private final FreePostRepository freePostRepository;
    private final FreePostCommentRepository freePostCommentRepository;
    private final FreePostLikeRepository freePostLikeRepository;
    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public CursorPageResponse<FreePostSummaryResponse> getPosts(
            Long cursorId, String keyword, int size, FreePostCategory category, FreePostSort sort
    ) {
        return getPostsByStatus(cursorId, keyword, size, null, category, sort);
    }

    public CursorPageResponse<FreePostSummaryResponse> getPreDispatchPosts(
            Long cursorId, String keyword, int size, FreePostCategory category, FreePostSort sort
    ) {
        return getPostsByStatus(cursorId, keyword, size, "파견 전", category, sort);
    }

    public CursorPageResponse<FreePostSummaryResponse> getDispatchedPosts(
            Long cursorId, String keyword, int size, FreePostCategory category, FreePostSort sort
    ) {
        return getPostsByStatus(cursorId, keyword, size, "파견 중", category, sort);
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
                        freePostCommentRepository.countByFreePostIdAndDeletedFalse(post.getId())
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
                        freePostCommentRepository.countByFreePostIdAndDeletedFalse(post.getId())
                ))
                .toList();

        Long nextCursorId = hasNext ? pagePosts.get(pagePosts.size() - 1).getId() : null;
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }

    public FreePostDetailResponse getPost(Long memberId, Long postId) {
        FreePost post = getPostEntity(postId);
        List<FreePostComment> all = freePostCommentRepository.findByFreePostIdOrderByIdAsc(postId);
        List<FreePostCommentResponse> comments = toCommentTree(all, memberId);
        long commentCount = all.stream().filter(comment -> !comment.isDeleted()).count();

        return FreePostDetailResponse.from(
                post,
                freePostLikeRepository.countByFreePostId(postId),
                scrapRepository.countByTargetTypeAndTargetId(ScrapTargetType.FREE_POST, postId),
                commentCount,
                freePostLikeRepository.existsByFreePostIdAndMemberId(postId, memberId),
                memberId,
                comments
        );
    }

    /**
     * 일반 댓글 아래에 답글을 묶는다. 둘 다 작성 순서대로다.
     * 삭제된 댓글은 답글이 남아 있을 때만 자리를 보여준다.
     */
    private List<FreePostCommentResponse> toCommentTree(List<FreePostComment> all, Long memberId) {
        Map<Long, List<FreePostCommentResponse>> repliesByParentId = new HashMap<>();
        for (FreePostComment comment : all) {
            if (comment.isReply()) {
                repliesByParentId.computeIfAbsent(comment.getParent().getId(), id -> new ArrayList<>())
                        .add(FreePostCommentResponse.from(comment, memberId));
            }
        }

        return all.stream()
                .filter(comment -> !comment.isReply())
                .map(comment -> FreePostCommentResponse.from(
                        comment, memberId, repliesByParentId.getOrDefault(comment.getId(), List.of())))
                .filter(response -> !response.deleted() || !response.replies().isEmpty())
                .toList();
    }

    @Transactional
    public Long createPost(Long memberId, FreePostRequest request) {
        Member member = getMember(memberId);
        FreePost post = FreePost.builder()
                .member(member)
                .title(request.title().trim())
                .content(request.content().trim())
                .country(FreePost.resolveCountry(member))
                .status(FreePost.resolveStatus(member))
                .category(request.category() == null ? FreePostCategory.CHAT : request.category())
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
                FreePost.resolveCountry(post.getMember()),
                FreePost.resolveStatus(post.getMember()),
                request.category(),
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
        FreePostComment parent = request.parentId() != null ? getReplyTarget(postId, request.parentId()) : null;

        FreePostComment comment = freePostCommentRepository.save(FreePostComment.builder()
                .freePost(post)
                .member(member)
                .content(request.content().trim())
                .parent(parent)
                .build());

        notificationService.notifyPostComment(
                post.getMember().getId(),
                post.getId(),
                post.getTitle(),
                memberId,
                comment.getContent()
        );

        return FreePostCommentResponse.from(comment, memberId);
    }

    /**
     * 답글이 달린 댓글은 "삭제된 댓글입니다"로 자리만 남긴다. 답글이 없으면 행째 지운다.
     * 답글을 지워서 삭제된 원댓글에 남은 답글이 없어지면 원댓글 자리도 함께 지운다.
     */
    @Transactional
    public void deleteComment(Long memberId, Long postId, Long commentId) {
        FreePostComment comment = getComment(postId, commentId);
        validateOwner(comment.getMember().getId(), memberId);

        if (!comment.isReply() && freePostCommentRepository.existsByParentId(comment.getId())) {
            comment.markDeleted();
            return;
        }

        FreePostComment parent = comment.getParent();
        freePostCommentRepository.delete(comment);
        freePostCommentRepository.flush();

        if (parent != null && parent.isDeleted() && !freePostCommentRepository.existsByParentId(parent.getId())) {
            freePostCommentRepository.delete(parent);
        }
    }

    @Transactional
    public FreePostCommentResponse updateComment(Long memberId, Long postId, Long commentId, FreePostCommentRequest request) {
        FreePostComment comment = getComment(postId, commentId);
        validateOwner(comment.getMember().getId(), memberId);

        comment.updateContent(request.content().trim());
        return FreePostCommentResponse.from(comment, memberId);
    }

    /** 이 글에 달린, 지워지지 않은 댓글. 다른 글의 댓글 ID로 접근하면 없는 것으로 본다. */
    private FreePostComment getComment(Long postId, Long commentId) {
        FreePostComment comment = freePostCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getFreePost().getId().equals(postId) || comment.isDeleted()) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }

    /** 답글을 달 원댓글. 답글에는 답글을 달 수 없다. */
    private FreePostComment getReplyTarget(Long postId, Long parentId) {
        FreePostComment parent = freePostCommentRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        if (!parent.getFreePost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (parent.isReply()) {
            throw new CustomException(ErrorCode.REPLY_TO_REPLY_NOT_ALLOWED);
        }
        if (parent.isDeleted()) {
            throw new CustomException(ErrorCode.REPLY_TO_DELETED_COMMENT);
        }
        return parent;
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

    private CursorPageResponse<FreePostSummaryResponse> getPostsByStatus(
            Long cursorId, String keyword, int size, String status, FreePostCategory category, FreePostSort sort
    ) {
        int requestSize = normalizeSize(size);
        String normalizedKeyword = normalizeKeyword(keyword);
        PageRequest pageable = PageRequest.of(0, requestSize + 1);

        List<FreePost> posts = switch (sort == null ? FreePostSort.LATEST : sort) {
            case LATEST -> freePostRepository.findLatestByCursor(cursorId, normalizedKeyword, status, category, pageable);
            case OLDEST -> freePostRepository.findOldestByCursor(cursorId, normalizedKeyword, status, category, pageable);
            case POPULAR -> freePostRepository.findPopularByCursor(
                    cursorId,
                    cursorId == null ? 0L : freePostLikeRepository.countByFreePostId(cursorId),
                    normalizedKeyword,
                    status,
                    category,
                    pageable
            );
        };
        return toCursorResponse(posts, requestSize);
    }
}
