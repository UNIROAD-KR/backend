package com.uniroad.backend.domain.blog.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * 에디터가 보낸 HTML을 그대로 믿지 않기 위한 소독기.
 *
 * 글은 관리자만 쓰지만, 저장된 HTML은 모든 방문자에게 그대로 렌더된다.
 * 관리자 계정이 한 번 털리면 전체 방문자에게 스크립트가 나가므로
 * "누가 보냈는지"가 아니라 "무엇을 허용할지"로 막는다.
 *
 * style 속성은 통째로 막는다. 하이라이트 색은 style이 아니라 data-color로 싣고
 * 실제 색은 프론트 CSS가 입힌다(허용 목록을 좁게 유지하려는 의도적인 선택).
 */
@Component
public class BlogContentSanitizer {

    private static final Safelist SAFELIST = buildSafelist();

    private static Safelist buildSafelist() {
        return Safelist.none()
                .addTags(
                        "p", "br", "hr",
                        "h1", "h2", "h3", "h4",
                        "strong", "b", "em", "i", "u", "s", "del", "mark", "code",
                        "ul", "ol", "li",
                        "blockquote", "pre",
                        "a", "img"
                )
                .addAttributes("a", "href", "title", "target")
                .addAttributes("img", "src", "alt", "title", "width", "height")
                // 하이라이트 색 구분용. 값은 아래 renderHTML 규칙에 맞춰 프론트에서 CSS로 처리한다.
                .addAttributes("mark", "data-color")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https")
                // 외부 링크는 새 창으로 열되 opener를 넘기지 않는다
                .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer");
    }

    /** 허용 목록에 없는 태그·속성을 걷어낸 HTML을 돌려준다 */
    public String sanitize(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return "";
        }
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        String cleaned = Jsoup.clean(rawHtml, "", SAFELIST, settings);

        // 소독 과정에서 속성만 떨어져 나가 껍데기가 남는 경우를 정리한다.
        // (예: onerror만 있던 img → <img>, javascript: 링크 → href 없는 a)
        Document document = Jsoup.parseBodyFragment(cleaned);
        document.outputSettings(settings);
        document.select("img:not([src])").remove();
        document.select("a:not([href])").forEach(org.jsoup.nodes.Element::unwrap);
        return document.body().html();
    }

    /** 태그를 걷어낸 본문. 요약 자동 생성에 쓴다. */
    public String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parse(html).text().replaceAll("\\s+", " ").trim();
    }

    /** 썸네일을 지정하지 않았을 때 쓸 본문 첫 이미지 */
    public String firstImageUrl(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        return Jsoup.parse(html).select("img[src]").stream()
                .map(element -> element.attr("src"))
                .filter(src -> !src.isBlank())
                .findFirst()
                .orElse(null);
    }
}
