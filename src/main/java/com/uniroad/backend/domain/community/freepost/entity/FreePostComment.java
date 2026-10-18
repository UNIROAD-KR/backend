package com.uniroad.backend.domain.community.freepost.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "free_post_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FreePostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_post_id", nullable = false)
    private FreePost freePost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 답글이면 원댓글, 일반 댓글이면 null. 답글에는 답글을 달 수 없어 깊이는 한 단계뿐이다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FreePostComment parent;

    /**
     * 답글이 달린 댓글을 지우면 행을 남기고 "삭제된 댓글입니다"로 보여준다. 답글이 없으면 행째 지운다.
     * 이미 댓글이 있는 테이블에 not null 컬럼을 더하는 것이라 기본값을 함께 준다.
     */
    @Column(nullable = false, columnDefinition = "boolean default false not null")
    @Builder.Default
    private boolean deleted = false;

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean isReply() {
        return parent != null;
    }

    public void markDeleted() {
        this.deleted = true;
    }
}
