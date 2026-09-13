package com.uniroad.backend.domain.blog.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 소독기가 지켜야 할 두 가지를 고정한다.
 *
 * - 안전: 허용하지 않은 태그·프로토콜은 어떤 경우에도 살아남지 않는다.
 * - 검색 노출: 내부 링크와 제목 단계는 검색엔진이 읽는 신호라 형태가 정해져 있다.
 *
 * 특히 내부 링크는 예전에 두 번 망가져 있었다. 모든 a에 nofollow가 강제로 붙었고,
 * baseUri가 없어 상대 경로 href는 프로토콜 검사에 걸려 통째로 지워졌다.
 */
class BlogContentSanitizerTest {

    private final BlogContentSanitizer sanitizer = new BlogContentSanitizer("https://uniroad.kr");

    /* ── 안전 ───────────────────────────────────────── */

    @Test
    @DisplayName("스크립트와 이벤트 핸들러는 남지 않는다")
    void removesScriptAndEventHandlers() {
        String result = sanitizer.sanitize(
                "<p onclick=\"steal()\">글</p><script>alert(1)</script><img src=x onerror=\"alert(1)\">");

        assertThat(result).doesNotContain("script", "onclick", "onerror");
        assertThat(result).contains("글");
    }

    @Test
    @DisplayName("javascript: 링크는 href가 지워지고 껍데기 a도 남지 않는다")
    void stripsJavascriptLinks() {
        String result = sanitizer.sanitize("<p><a href=\"javascript:alert(1)\">누르지 마세요</a></p>");

        assertThat(result).doesNotContain("javascript:", "<a");
        assertThat(result).contains("누르지 마세요");
    }

    @Test
    @DisplayName("style 속성은 허용하지 않는다")
    void dropsStyleAttribute() {
        String result = sanitizer.sanitize("<p style=\"position:fixed\">글</p>");

        assertThat(result).doesNotContain("style");
    }

    /* ── 내부·외부 링크 ──────────────────────────────── */

    @Test
    @DisplayName("상대 경로 내부 링크는 href가 살아남고 nofollow가 붙지 않는다")
    void keepsRelativeInternalLinkWithoutNofollow() {
        String result = sanitizer.sanitize("<p><a href=\"/blog/other-post\">다른 글</a></p>");

        assertThat(result).contains("href=\"/blog/other-post\"");
        assertThat(result).doesNotContain("nofollow");
        // 사이트 안에서 이동하는 링크를 새 창으로 열 이유가 없다
        assertThat(result).doesNotContain("target");
    }

    @Test
    @DisplayName("우리 도메인을 절대 주소로 적어도 내부 링크로 본다")
    void treatsOwnDomainAsInternal() {
        String result = sanitizer.sanitize("<p><a href=\"https://www.uniroad.kr/faq\">FAQ</a></p>");

        assertThat(result).doesNotContain("nofollow");
    }

    @Test
    @DisplayName("외부 링크에는 nofollow와 새 창이 붙는다")
    void marksExternalLinks() {
        String result = sanitizer.sanitize("<p><a href=\"https://example.com\">바깥</a></p>");

        assertThat(result).contains("rel=\"nofollow noopener noreferrer\"");
        assertThat(result).contains("target=\"_blank\"");
    }

    @Test
    @DisplayName("에디터가 붙여둔 rel을 외부 링크에서 덮어쓴다")
    void overridesIncomingRelOnExternalLinks() {
        String result = sanitizer.sanitize("<p><a href=\"https://example.com\" rel=\"dofollow\">바깥</a></p>");

        assertThat(result).doesNotContain("dofollow");
        assertThat(result).contains("nofollow");
    }

    @Test
    @DisplayName("문서 안 앵커는 내부 링크다")
    void treatsAnchorAsInternal() {
        String result = sanitizer.sanitize("<p><a href=\"#section\">아래로</a></p>");

        assertThat(result).contains("href=\"#section\"");
        assertThat(result).doesNotContain("nofollow");
    }

    /* ── 제목 단계 ──────────────────────────────────── */

    @Test
    @DisplayName("본문 h1은 h2로 내려 페이지의 H1과 겹치지 않게 한다")
    void demotesH1ToH2() {
        String result = sanitizer.sanitize("<h1>본문 제목</h1><h2>소제목</h2>");

        assertThat(result).doesNotContain("<h1");
        assertThat(result).contains("<h2>본문 제목</h2>");
        assertThat(result).contains("<h2>소제목</h2>");
    }

    /* ── 부수 기능 ──────────────────────────────────── */

    @Test
    @DisplayName("이미지 크기 속성은 보존한다 — 레이아웃 밀림을 막는 값이다")
    void keepsImageDimensions() {
        String result = sanitizer.sanitize(
                "<p><img src=\"https://cdn.example.com/a.png\" alt=\"설명\" width=\"800\" height=\"600\"></p>");

        assertThat(result).contains("width=\"800\"", "height=\"600\"", "alt=\"설명\"");
    }

    @Test
    @DisplayName("첫 이미지 주소를 찾아낸다")
    void findsFirstImage() {
        String html = sanitizer.sanitize(
                "<p>글</p><p><img src=\"https://cdn.example.com/a.png\"></p>"
                        + "<p><img src=\"https://cdn.example.com/b.png\"></p>");

        assertThat(sanitizer.firstImageUrl(html)).isEqualTo("https://cdn.example.com/a.png");
    }

    @Test
    @DisplayName("태그를 걷어낸 본문을 돌려준다")
    void extractsPlainText() {
        String result = sanitizer.toPlainText("<h2>제목</h2><p>본문   입니다</p>");

        assertThat(result).isEqualTo("제목 본문 입니다");
    }

    @Test
    @DisplayName("빈 입력은 빈 문자열이다")
    void handlesBlankInput() {
        assertThat(sanitizer.sanitize(null)).isEmpty();
        assertThat(sanitizer.sanitize("  ")).isEmpty();
        assertThat(sanitizer.toPlainText(null)).isEmpty();
        assertThat(sanitizer.firstImageUrl(null)).isNull();
    }
}
