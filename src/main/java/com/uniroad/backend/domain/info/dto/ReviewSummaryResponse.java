package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.Review;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewSummaryResponse(
        Long id,
        String title,
        String summary,
        BigDecimal rating,
        String authorNickname,
        LocalDateTime createdAt
) {
    public static ReviewSummaryResponse from(Review review) {
        return new ReviewSummaryResponse(
                review.getId(),
                review.getTitle(),
                review.getSummary(),
                review.getRating(),
                review.getAuthorNickname(),
                review.getCreatedAt()
        );
    }
}
