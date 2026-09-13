package com.uniroad.backend.domain.blog.entity;

import java.util.Map;

/**
 * 글의 화면용 내용 한 벌.
 *
 * 예전에는 이 일곱 값을 update() 인자로 늘어놓았는데, SEO 필드까지 더하면 열세 개가 된다.
 * String이 연달아 있어 순서를 한 칸 밀려 넘겨도 컴파일이 통과하므로 묶어서 넘긴다.
 */
public record BlogPostContent(
        String slug,
        String title,
        String summary,
        String thumbnailUrl,
        Map<String, Object> contentJson,
        String contentHtml,
        String plainText
) {
}
