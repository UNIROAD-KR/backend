package com.uniroad.backend.domain.community.freepost.dto;

import com.uniroad.backend.domain.community.freepost.entity.FreePost;

import java.time.LocalDateTime;
import java.util.List;

public record FreePostDetailResponse(
        Long id,
        String title,
        String content,
        String country,
        String status,
        String authorName,
        List<String> imageUrls,
        long likeCount,
        long commentCount,
        boolean liked,
        boolean mine,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FreePostCommentResponse> comments
) {
    public static FreePostDetailResponse from(
            FreePost post,
            long likeCount,
            long commentCount,
            boolean liked,
            Long memberId,
            List<FreePostCommentResponse> comments
    ) {
        return new FreePostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCountry(),
                post.getStatus(),
                "익명",
                post.getImageUrls() == null ? List.of() : post.getImageUrls(),
                likeCount,
                commentCount,
                liked,
                post.getMember().getId().equals(memberId),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                comments
        );
    }
}
