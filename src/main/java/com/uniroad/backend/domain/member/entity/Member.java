package com.uniroad.backend.domain.member.entity;

import com.uniroad.backend.domain.verification.entity.Verification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 회원 엔티티
 *
 * - 일반 로그인: provider = "LOCAL", password 존재
 * - 소셜 로그인: provider = "KAKAO" | "NAVER", password = null
 * - 소셜 계정 연동: 기존 일반 회원에 provider/providerId 추가
 */
@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_provider", columnList = "provider, provider_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    // JWT 기반 일반 로그인 사용자를 위한 비밀번호 (OAuth2 가입자는 null)
    private String password;

    @Column(nullable = false)
    private String name;

    private Integer age;

    @Column(name = "dispatched_university")
    private String dispatchedUniversity;

    @Column(name = "dispatched_country")
    private String dispatchedCountry;

    @Column(name = "dispatched_region")
    private String dispatchedRegion;

    // KAKAO, NAVER, LOCAL 등 로그인 제공자 정보
    @Builder.Default
    private String provider = "LOCAL";

    // 소셜 로그인에서 제공해주는 고유 ID (일반 로그인은 null)
    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.NEED_SIGNUP;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Verification> verifications = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateStatus(MemberStatus status) {
        this.status = status;
    }

    public void completeOnboarding(Integer age, String dispatchedUniversity, String dispatchedCountry, String dispatchedRegion) {
        this.age = age;
        this.dispatchedUniversity = dispatchedUniversity;
        this.dispatchedCountry = dispatchedCountry;
        this.dispatchedRegion = dispatchedRegion;
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 일반 가입 회원이 소셜 로그인 최초 시도 시 계정 연동
     */
    public void linkOAuth2(String provider, String providerId) {
        this.provider   = provider;
        this.providerId = providerId;
    }

    /**
     * 비밀번호 변경 (인코딩 완료된 값을 전달받아 저장)
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void chargeBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void spendBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new com.uniroad.backend.global.exception.CustomException(
                com.uniroad.backend.global.exception.ErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);
    }
}
