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
import java.util.LinkedHashMap;
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

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Builder
    private BlogPost(
            String slug,
            String title,
            String summary,
            String thumbnailUrl,
            Map<String, Object> contentJson,
            String contentHtml,
            String plainText,
            BlogPostStatus status,
            Member author
    ) {
        this.slug = slug;
        this.title = title;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.contentJson = contentJson == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contentJson);
        this.contentHtml = contentHtml;
        this.plainText = plainText;
        this.status = status == null ? BlogPostStatus.DRAFT : status;
        this.author = author;
        this.viewCount = 0L;
        if (this.status == BlogPostStatus.PUBLISHED) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void update(
            String slug,
            String title,
            String summary,
            String thumbnailUrl,
            Map<String, Object> contentJson,
            String contentHtml,
            String plainText
    ) {
        this.slug = slug;
        this.title = title;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.contentJson = contentJson == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contentJson);
        this.contentHtml = contentHtml;
        this.plainText = plainText;
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

    public void increaseViewCount() {
        this.viewCount++;
    }

    public boolean isPublished() {
        return this.status == BlogPostStatus.PUBLISHED;
    }
}
