package com.uniroad.backend.domain.notification.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcm_token", indexes = {
        @Index(name = "idx_fcm_token_member", columnList = "member_id"),
        @Index(name = "uk_fcm_token_value", columnList = "token", unique = true)
})
public class FcmToken extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String token;

    private FcmToken(Member member, String token) {
        this.member = member;
        this.token = token;
    }

    public static FcmToken create(Member member, String token) {
        return new FcmToken(member, token);
    }

    public void updateMember(Member member) {
        this.member = member;
    }
}
