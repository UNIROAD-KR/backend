package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ExchangeReviewResponse(
        Long id,
        String title,
        String content,
        String country,
        String type,
        String authorName,
        LocalDateTime createdAt,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        List<String> tags,
        boolean likedByMe
) {
    public static ExchangeReviewResponse from(
            Review review,
            long likeCount,
            long commentCount,
            boolean likedByMe
    ) {
        return new ExchangeReviewResponse(
                review.getId(),
                review.getTitle(),
                review.getContent() != null ? review.getContent() : review.getSummary(),
                review.getPartnerUniversity().getCountry().getName(),
                review.getType(),
                review.getAuthorNickname() != null ? review.getAuthorNickname() : getMemberName(review),
                review.getCreatedAt(),
                likeCount,
                commentCount,
                review.getViewCount(),
                review.getTags() == null ? List.of() : review.getTags(),
                likedByMe
        );
    }

    private static String getMemberName(Review review) {
        return review.getMember() == null ? null : review.getMember().getName();
    }
}
