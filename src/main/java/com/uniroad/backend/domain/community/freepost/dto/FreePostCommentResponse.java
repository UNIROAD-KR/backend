package com.uniroad.backend.domain.community.freepost.dto;

import com.uniroad.backend.domain.community.freepost.entity.FreePostComment;

import java.time.LocalDateTime;

public record FreePostCommentResponse(
        Long id,
        String authorName,
        String content,
        LocalDateTime createdAt,
        boolean mine
) {
    public static FreePostCommentResponse from(FreePostComment comment, Long memberId) {
        return new FreePostCommentResponse(
                comment.getId(),
                "익명",
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getMember().getId().equals(memberId)
        );
    }
}
