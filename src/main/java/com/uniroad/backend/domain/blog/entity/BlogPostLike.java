package com.uniroad.backend.domain.blog.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 한 회원이 한 글에 한 번만 누를 수 있도록 유니크 제약으로 막는다 */
@Entity
@Table(name = "blog_post_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_blog_post_like_post_member", columnNames = {"blog_post_id", "member_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogPostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id", nullable = false)
    private BlogPost blogPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    private BlogPostLike(BlogPost blogPost, Member member) {
        this.blogPost = blogPost;
        this.member = member;
    }
}
