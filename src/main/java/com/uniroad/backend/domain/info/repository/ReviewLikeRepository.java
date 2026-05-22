package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId);

    long countByReviewId(Long reviewId);

    Optional<ReviewLike> findByReviewIdAndMemberId(Long reviewId, Long memberId);
}
