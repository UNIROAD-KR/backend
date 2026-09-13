package com.uniroad.backend.domain.blog.dto;

import com.uniroad.backend.domain.blog.entity.BlogPost;

import java.time.LocalDateTime;

/** 목록 카드에 필요한 것만 담는다. 본문(HTML/JSON)은 넣지 않는다. */
public record BlogPostSummaryResponse(
        Long id,
        String slug,
        String title,
        String summary,
        String thumbnailUrl,
        String authorNickname,
        String status,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long viewCount,
        long likeCount,
        boolean likedByMe,
        /** sitemap이 색인 제외 글을 걸러내는 데 쓴다. 나머지 SEO 값은 목록에 싣지 않는다. */
        boolean noindex
) {
    public static BlogPostSummaryResponse of(BlogPost post, long likeCount, boolean likedByMe) {
        return new BlogPostSummaryResponse(
                post.getId(),
                post.getSlug(),
                post.getTitle(),
                post.getSummary(),
                post.getThumbnailUrl(),
                post.getAuthor().getNickname(),
                post.getStatus().name(),
                post.getPublishedAt(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount(),
                likeCount,
                likedByMe,
                post.isNoindex()
        );
    }
}
