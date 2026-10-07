package com.uniroad.backend.domain.community.freepost.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.CurrentSituation;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "free_post", indexes = {
        @Index(name = "idx_free_post_created_at", columnList = "created_at"),
        @Index(name = "idx_free_post_country", columnList = "country")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FreePost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String status;

    // 이미 글이 있는 테이블에 not null 컬럼을 더하는 것이라 기본값을 함께 준다. 카테고리가 생기기 전 글은 사담글로 본다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'CHAT' not null")
    @Builder.Default
    private FreePostCategory category = FreePostCategory.CHAT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    public void update(String title, String content, String country, String status, FreePostCategory category, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.country = country;
        this.status = status;
        if (category != null) {
            this.category = category;
        }
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    /**
     * 파견 국가는 온보딩에서 "파견교 미정"을 고를 수 있어 비어 있을 수 있다.
     * country 컬럼은 NOT NULL이므로 그대로 두면 글 작성이 500으로 실패한다.
     */
    public static String resolveCountry(Member member) {
        String dispatchedCountry = member.getDispatchedCountry();
        return dispatchedCountry == null || dispatchedCountry.isBlank() ? "미정" : dispatchedCountry;
    }

    public static String resolveStatus(Member member) {
        return member.getCurrentSituation() == CurrentSituation.DISPATCHED ? "파견 중" : "파견 전";
    }
}
