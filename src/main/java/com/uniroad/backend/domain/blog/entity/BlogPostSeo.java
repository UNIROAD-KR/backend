package com.uniroad.backend.domain.blog.entity;

import java.util.List;

/**
 * 검색 노출에만 쓰는 값 한 벌.
 *
 * 전부 "작성자가 직접 적은 것"만 담는다. 비어 있으면 비어 있는 채로 저장하고,
 * 화면용 값으로 대신 채우는 일은 응답을 만들 때 한다.
 */
public record BlogPostSeo(
        String metaTitle,
        String metaDescription,
        String ogImageUrl,
        List<String> tags,
        String canonicalUrl,
        boolean noindex
) {
    public static BlogPostSeo empty() {
        return new BlogPostSeo(null, null, null, List.of(), null, false);
    }
}
