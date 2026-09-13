package com.uniroad.backend.domain.blog.dto;

import com.uniroad.backend.domain.blog.entity.BlogPost;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * contentJson은 관리자가 수정 화면에서 에디터를 복원할 때만 쓴다.
 * 공개 상세 조회에서는 굳이 실어 보내지 않는다(응답 크기가 두 배가 된다).
 *
 * SEO 값은 두 벌로 내려간다.
 * - metaTitle 등        : 작성자가 직접 적은 것. 비었으면 null이다. 수정 화면이 이걸 편집한다.
 * - effectiveMetaTitle 등: 비었을 때 화면용 값으로 채운 결과. 페이지는 이쪽만 본다.
 *
 * 채운 값을 DB에 저장하지 않고 여기서 계산하는 이유는, 저장해 버리면 "작성자가 비워둔 것"과
 * "직접 그렇게 쓴 것"을 구별할 수 없게 되고 제목이나 본문을 고쳐도 옛 값이 남기 때문이다.
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
        boolean likedByMe,

        // 작성자가 적은 그대로
        String metaTitle,
        String metaDescription,
        String ogImageUrl,
        List<String> tags,
        String canonicalUrl,
        boolean noindex,

        // 비었을 때를 메운 결과
        String effectiveMetaTitle,
        String effectiveMetaDescription,
        String effectiveOgImageUrl
) {
    public static BlogPostDetailResponse of(BlogPost post, long likeCount, boolean likedByMe, boolean includeJson) {
        return of(post, likeCount, likedByMe, includeJson, post.getViewCount());
    }

    /** 조회수를 따로 받는 쪽 — 조회 시점의 +1이 엔티티에는 반영되지 않기 때문이다 */
    public static BlogPostDetailResponse of(
            BlogPost post, long likeCount, boolean likedByMe, boolean includeJson, long viewCount) {
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
                viewCount,
                likeCount,
                likedByMe,
                post.getMetaTitle(),
                post.getMetaDescription(),
                post.getOgImageUrl(),
                post.getTags(),
                post.getCanonicalUrl(),
                post.isNoindex(),
                firstNonBlank(post.getMetaTitle(), post.getTitle()),
                firstNonBlank(post.getMetaDescription(), post.getSummary()),
                firstNonBlank(post.getOgImageUrl(), post.getThumbnailUrl())
        );
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return (fallback != null && !fallback.isBlank()) ? fallback : null;
    }
}
