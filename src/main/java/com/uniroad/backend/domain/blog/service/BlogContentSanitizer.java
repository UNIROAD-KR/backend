package com.uniroad.backend.domain.blog.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

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

    /** 본문에 h1이 있으면 페이지 제목과 H1이 겹친다. 검색엔진에는 글의 주제가 둘로 보인다. */
    private static final String DEMOTED_HEADING = "h2";

    /** 우리 도메인 바깥으로 나가는 링크에만 붙인다 */
    private static final String EXTERNAL_LINK_REL = "nofollow noopener noreferrer";

    private final String siteUrl;
    private final String siteHost;

    public BlogContentSanitizer(@Value("${frontend.url}") String frontendUrl) {
        this.siteUrl = frontendUrl;
        this.siteHost = hostOf(frontendUrl);
    }

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
                // 상대 경로(/blog/...)를 살리려면 절대 URL만 허용하는 기본 동작을 꺼야 한다.
                // 프로토콜 제한은 그대로라 javascript: 는 여전히 걸러진다.
                .preserveRelativeLinks(true)
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https");
    }

    /** 허용 목록에 없는 태그·속성을 걷어낸 HTML을 돌려준다 */
    public String sanitize(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return "";
        }
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        // baseUri를 우리 주소로 준다. Jsoup은 프로토콜 검사 전에 상대 경로를 절대 URL로 풀어보는데,
        // 기준 주소가 없으면 "/blog/..." 가 어떤 프로토콜에도 걸리지 않아 href째로 잘려나간다.
        // preserveRelativeLinks(true)라 검사만 절대 URL로 하고 저장되는 값은 상대 경로 그대로다.
        String cleaned = Jsoup.clean(rawHtml, siteUrl, SAFELIST, settings);

        // 소독 과정에서 속성만 떨어져 나가 껍데기가 남는 경우를 정리한다.
        // (예: onerror만 있던 img → <img>, javascript: 링크 → href 없는 a)
        Document document = Jsoup.parseBodyFragment(cleaned);
        document.outputSettings(settings);
        document.select("img:not([src])").remove();
        document.select("a:not([href])").forEach(Element::unwrap);

        demoteTopHeadings(document);
        applyLinkRel(document);

        return document.body().html();
    }

    /**
     * 본문의 h1을 h2로 내린다.
     * 글 제목은 화면에서 이미 h1으로 그려지므로, 본문에도 h1이 있으면 한 문서에 H1이 둘이 된다.
     */
    private void demoteTopHeadings(Document document) {
        document.select("h1").forEach(heading -> heading.tagName(DEMOTED_HEADING));
    }

    /**
     * 외부 링크에만 nofollow를 붙인다.
     *
     * 예전에는 Safelist가 모든 a에 강제로 붙였는데, 그러면 우리 글에서 우리 페이지로 건 링크까지
     * "따라가지 말라"고 표시하는 셈이라 사이트 안에서 권위가 전달되지 않는다.
     * 내부 링크는 rel을 비우고 새 창도 열지 않는다.
     */
    private void applyLinkRel(Document document) {
        for (Element link : document.select("a[href]")) {
            if (isInternal(link.attr("href"))) {
                link.removeAttr("rel");
                link.removeAttr("target");
            } else {
                link.attr("rel", EXTERNAL_LINK_REL);
                link.attr("target", "_blank");
            }
        }
    }

    /** 상대 경로이거나 우리 도메인을 가리키면 내부 링크다 */
    private boolean isInternal(String href) {
        if (href.isBlank()) {
            return false;
        }
        // mailto:, tel: 같은 것은 내부로 볼 수 없다
        if (href.startsWith("#") || href.startsWith("/")) {
            return true;
        }
        String host = hostOf(href);
        return host != null && siteHost != null && host.equals(siteHost);
    }

    /** 호스트만 뽑는다. www는 같은 사이트로 본다. */
    private static String hostOf(String url) {
        try {
            String host = new URI(url).getHost();
            if (host == null) {
                return null;
            }
            host = host.toLowerCase(Locale.ROOT);
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
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
