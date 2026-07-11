package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);
    long countByReviewId(Long reviewId);
    java.util.Optional<ReviewLike> findByReviewIdAndMemberId(Long reviewId, Long memberId);
    void deleteByReviewId(Long reviewId);
    void deleteByMemberId(Long memberId);
}
