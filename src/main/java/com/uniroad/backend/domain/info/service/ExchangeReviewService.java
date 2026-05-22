package com.uniroad.backend.domain.info.service;

import com.uniroad.backend.domain.info.dto.ExchangeReviewResponse;
import com.uniroad.backend.domain.info.dto.ReviewCommentRequest;
import com.uniroad.backend.domain.info.dto.ReviewCommentResponse;
import com.uniroad.backend.domain.info.dto.ReviewLikeResponse;
import com.uniroad.backend.domain.info.entity.Review;
import com.uniroad.backend.domain.info.entity.ReviewComment;
import com.uniroad.backend.domain.info.entity.ReviewLike;
import com.uniroad.backend.domain.info.repository.ReviewCommentRepository;
import com.uniroad.backend.domain.info.repository.ReviewLikeRepository;
import com.uniroad.backend.domain.info.repository.ReviewRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberRepository memberRepository;

    public Page<ExchangeReviewResponse> getReviews(String keyword, String country, String type, Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return reviewRepository.search(null, normalize(country), normalize(type), normalize(keyword), pageable)
                .map(review -> toResponse(review, memberId));
    }

    @Transactional
    public ExchangeReviewResponse getReview(Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Review review = getReviewEntity(id);
        review.increaseViewCount();
        return toResponse(review, memberId);
    }

    public List<ReviewCommentResponse> getComments(Long reviewId) {
        ensureReviewExists(reviewId);
        return reviewCommentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId).stream()
                .map(ReviewCommentResponse::from)
                .toList();
    }

    @Transactional
    public ReviewCommentResponse createComment(Long reviewId, ReviewCommentRequest request) {
        Review review = getReviewEntity(reviewId);
        Member member = getCurrentMember();

        ReviewComment comment = reviewCommentRepository.save(ReviewComment.builder()
                .review(review)
                .member(member)
                .content(request.content().trim())
                .build());

        return ReviewCommentResponse.from(comment);
    }

    @Transactional
    public ReviewLikeResponse like(Long reviewId) {
        Review review = getReviewEntity(reviewId);
        Member member = getCurrentMember();

        if (!reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, member.getId())) {
            reviewLikeRepository.save(ReviewLike.builder()
                    .review(review)
                    .member(member)
                    .build());
        }

        return new ReviewLikeResponse(true, reviewLikeRepository.countByReviewId(reviewId));
    }

    @Transactional
    public ReviewLikeResponse unlike(Long reviewId) {
        ensureReviewExists(reviewId);
        Long memberId = SecurityUtil.getCurrentMemberId();
        reviewLikeRepository.findByReviewIdAndMemberId(reviewId, memberId)
                .ifPresent(reviewLikeRepository::delete);

        return new ReviewLikeResponse(false, reviewLikeRepository.countByReviewId(reviewId));
    }

    private ExchangeReviewResponse toResponse(Review review, Long memberId) {
        long likeCount = reviewLikeRepository.countByReviewId(review.getId());
        long commentCount = reviewCommentRepository.countByReviewId(review.getId());
        boolean likedByMe = reviewLikeRepository.existsByReviewIdAndMemberId(review.getId(), memberId);
        return ExchangeReviewResponse.from(review, likeCount, commentCount, likedByMe);
    }

    private Review getReviewEntity(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void ensureReviewExists(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    private Member getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
