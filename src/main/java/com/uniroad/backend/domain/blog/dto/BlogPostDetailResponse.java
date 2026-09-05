package com.uniroad.backend.domain.blog.dto;

import com.uniroad.backend.domain.blog.entity.BlogPost;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * contentJson은 관리자가 수정 화면에서 에디터를 복원할 때만 쓴다.
 * 공개 상세 조회에서는 굳이 실어 보내지 않는다(응답 크기가 두 배가 된다).
 */
public record BlogPostDetailResponse(
        Long id,
        String slug,
        String title,
        String summary,
        String thumbnailUrl,
        String contentHtml,
        Map<String, Object> contentJson,
        String authorNickname,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long viewCount,
        long likeCount,
        boolean likedByMe
) {
    public static BlogPostDetailResponse of(BlogPost post, long likeCount, boolean likedByMe, boolean includeJson) {
        return new BlogPostDetailResponse(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getSummary(),
                post.getThumbnailUrl(),
                post.getContentHtml(),
                includeJson ? post.getContentJson() : null,
                post.getAuthor().getNickname(),
                post.getStatus().name(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount(),
                likeCount,
                likedByMe
        );
    }
}
