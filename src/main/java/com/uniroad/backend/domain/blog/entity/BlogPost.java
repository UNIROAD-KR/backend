package com.uniroad.backend.domain.blog.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자가 쓰는 블로그 글.
 *
 * 본문을 JSON과 HTML 두 벌로 들고 있는 이유:
 * - contentJson : 에디터(ProseMirror)의 원본. 수정할 때 이걸로 복원해야 서식이 안 깨진다.
 * - contentHtml : 화면에 그대로 뿌리는 표현형. 자바에서 JSON을 HTML로 옮길 수단이 없어
 *                 클라이언트가 만든 HTML을 받되, 저장 전에 서버에서 반드시 소독한다.
 * - plainText   : 태그를 걷어낸 본문. 요약 자동 생성과 검색에 쓴다.
 */
@Entity
@Table(name = "blog_post", indexes = {
        @Index(name = "idx_blog_post_status_published_at", columnList = "status, published_at"),
        @Index(name = "idx_blog_post_slug", columnList = "slug", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 공개 URL(/blog/{slug})에 쓰는 식별자 */
    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    /** 목록 카드에 뜨는 설명. 비워두면 본문에서 만들어 채운다. */
    @Column(length = 300)
    private String summary;

    /** 목록 카드에 뜨는 이미지. 비워두면 본문 첫 이미지를 쓴다. */
    @Column(length = 500)
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "JSON")
    private Map<String, Object> contentJson;

    @Column(name = "content_html", nullable = false, columnDefinition = "LONGTEXT")
    private String contentHtml;

    @Column(name = "plain_text", columnDefinition = "LONGTEXT")
    private String plainText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogPostStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /* ── 검색 노출 ──────────────────────────────────────────
     * 비워두면 화면용 값(title·summary·thumbnailUrl)을 대신 쓴다.
     * 자동으로 채운 값을 컬럼에 박아 넣지 않는 이유는, 그 순간 "작성자가 비워둔 것"과
     * "직접 그렇게 쓴 것"을 구별할 수 없게 되고 본문을 고쳐도 옛 값이 남기 때문이다.
     * fallback은 응답을 만들 때 계산한다(BlogPostDetailResponse 참고).
     */

    /** 검색결과에 뜨는 제목. 비우면 title을 쓴다. */
    @Column(name = "meta_title", length = 60)
    private String metaTitle;

    /** 검색결과에 뜨는 설명. 비우면 summary를 쓴다. */
    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    /** 공유 미리보기 이미지(1200×630). 비우면 thumbnailUrl을 쓴다. */
    @Column(name = "og_image_url", length = 500)
    private String ogImageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "JSON")
    private List<String> tags;

    /** 같은 글을 외부에 먼저 실었을 때만 채운다. 비우면 /blog/{slug}가 원본이다. */
    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    /**
     * 색인에서 빼고 싶은 글.
     * 이미 행이 있는 테이블에 기본값 없는 not null을 추가하면 ddl-auto가 조용히 실패하므로
     * 기본값을 컬럼 정의에 직접 적는다.
     */
    @Column(name = "noindex", nullable = false, columnDefinition = "boolean not null default false")
    private boolean noindex;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Builder
    private BlogPost(BlogPostContent content, BlogPostSeo seo, BlogPostStatus status, Member author) {
        applyContent(content);
        applySeo(seo == null ? BlogPostSeo.empty() : seo);
        this.status = status == null ? BlogPostStatus.DRAFT : status;
        this.author = author;
        this.viewCount = 0L;
        if (this.status == BlogPostStatus.PUBLISHED) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void update(BlogPostContent content, BlogPostSeo seo) {
        applyContent(content);
        applySeo(seo == null ? BlogPostSeo.empty() : seo);
    }

    private void applyContent(BlogPostContent content) {
        this.slug = content.slug();
        this.title = content.title();
        this.summary = content.summary();
        this.thumbnailUrl = content.thumbnailUrl();
        this.contentJson = content.contentJson() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(content.contentJson());
        this.contentHtml = content.contentHtml();
        this.plainText = content.plainText();
    }

    private void applySeo(BlogPostSeo seo) {
        this.metaTitle = seo.metaTitle();
        this.metaDescription = seo.metaDescription();
        this.ogImageUrl = seo.ogImageUrl();
        this.tags = seo.tags() == null ? new ArrayList<>() : new ArrayList<>(seo.tags());
        this.canonicalUrl = seo.canonicalUrl();
        this.noindex = seo.noindex();
    }

    /** 예전에 저장된 글은 tags 컬럼이 null이다. 부르는 쪽에서 매번 확인하지 않게 여기서 흡수한다. */
    public List<String> getTags() {
        return this.tags == null ? List.of() : List.copyOf(this.tags);
    }

    /**
     * 최초 공개 시각만 기록한다. 내렸다가 다시 올려도 처음 공개한 날짜를 유지해야
     * 목록 정렬과 "언제 쓴 글"이 흔들리지 않는다.
     */
    public void publish() {
        this.status = BlogPostStatus.PUBLISHED;
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void unpublish() {
        this.status = BlogPostStatus.DRAFT;
    }

    /**
     * 제목이 전부 한글이면 slug를 만들 재료가 없다. 이때는 저장 후 id로 채우기 위해
     * 임시 slug로 먼저 저장하고 여기서 최종 값을 넣는다.
     */
    public void assignSlug(String slug) {
        this.slug = slug;
    }

    public boolean isPublished() {
        return this.status == BlogPostStatus.PUBLISHED;
    }
}
