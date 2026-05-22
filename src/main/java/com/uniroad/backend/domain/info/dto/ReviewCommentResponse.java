package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.ReviewComment;

import java.time.LocalDateTime;

public record ReviewCommentResponse(
        Long id,
        String authorName,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewCommentResponse from(ReviewComment comment) {
        return new ReviewCommentResponse(
                comment.getId(),
                comment.getMember() == null ? null : comment.getMember().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
