package com.uniroad.backend.domain.community.freepost.dto;

import com.uniroad.backend.domain.community.freepost.entity.FreePost;

import java.time.LocalDateTime;
import java.util.List;

public record FreePostSummaryResponse(
        Long id,
        String title,
        String preview,
        String country,
        String status,
        String authorName,
        long likeCount,
        long scrapCount,
        long commentCount,
        String thumbnailImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final int PREVIEW_LENGTH = 80;

    public static FreePostSummaryResponse from(FreePost post, long likeCount, long scrapCount, long commentCount) {
        return new FreePostSummaryResponse(
                post.getId(),
                post.getTitle(),
                toPreview(post.getContent()),
                post.getCountry(),
                post.getStatus(),
                "익명",
                likeCount,
                scrapCount,
                commentCount,
                firstImageUrl(post.getImageUrls()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private static String toPreview(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= PREVIEW_LENGTH ? normalized : normalized.substring(0, PREVIEW_LENGTH);
    }

    private static String firstImageUrl(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return imageUrls.get(0);
    }
}
