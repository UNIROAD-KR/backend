package com.uniroad.backend.domain.member.entity;

import com.uniroad.backend.domain.info.entity.University;
import com.uniroad.backend.domain.verification.entity.Verification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 회원 엔티티
 *
 * - 일반 로그인: provider = "LOCAL", password 존재
 * - 소셜 계정 식별 정보는 member_social_account 테이블에서 관리
 * - provider/providerId 컬럼은 기존 데이터 호환을 위해 유지
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

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_situation")
    private CurrentSituation currentSituation;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "dispatched_university")
    private String dispatchedUniversity;

    @Column(name = "dispatched_country")
    private String dispatchedCountry;

    @Column(name = "dispatched_region")
    private String dispatchedRegion;

    @Column(name = "dispatch_year")
    private Integer dispatchYear;

    @Column(name = "dispatch_semester")
    private String dispatchSemester;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "dispatch_start_date")
    private LocalDate dispatchStartDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domestic_university_id")
    private University domesticUniversity;

    // Legacy: 소셜 계정 식별은 MemberSocialAccount를 사용
    @Builder.Default
    private String provider = "LOCAL";

    // Legacy: 소셜 계정 식별은 MemberSocialAccount를 사용
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

    /**
     * 발급된 Access Token의 세대 번호.
     *
     * 토큰에 발급 시점의 값을 실어 두고 인증할 때 이 값과 대조한다.
     * 로그아웃·비밀번호 변경처럼 "지금까지 준 토큰을 전부 회수해야" 하는 순간에
     * 이 값을 1 올리면, 남은 수명이 얼마든 그 토큰들은 그 즉시 통하지 않는다.
     * 인증 필터가 어차피 요청마다 회원을 읽으므로 대조 비용은 0이다.
     */
    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;
    

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Verification> verifications = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateStatus(MemberStatus status) {
        this.status = status;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void completeOnboarding(Integer birthYear, University domesticUniversity, String dispatchedUniversity,
                                   String dispatchedCountry, String dispatchedRegion, String nickname,
                                   Gender gender, CurrentSituation currentSituation,
                                   Integer dispatchYear, String dispatchSemester,
                                   LocalDate applicationDeadline, LocalDate departureDate,
                                   LocalDate dispatchStartDate, LocalDate returnDate) {
        this.birthYear = birthYear;
        this.domesticUniversity = domesticUniversity;
        this.dispatchedUniversity = dispatchedUniversity;
        this.dispatchedCountry = dispatchedCountry;
        this.dispatchedRegion = dispatchedRegion;
        this.dispatchYear = dispatchYear;
        this.dispatchSemester = dispatchSemester;
        this.nickname = nickname;
        this.gender = gender;
        this.currentSituation = currentSituation;
        updateSituationDates(applicationDeadline, departureDate, dispatchStartDate, returnDate);
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 비밀번호 변경 (인코딩 완료된 값을 전달받아 저장)
     */
    public void updateProfile(CurrentSituation currentSituation, String nickname, String dispatchedUniversity,
                              String dispatchedCountry, String dispatchedRegion, University domesticUniversity,
                              Integer dispatchYear, String dispatchSemester,
                              LocalDate applicationDeadline, LocalDate departureDate,
                              LocalDate dispatchStartDate, LocalDate returnDate) {
        if (currentSituation != null) {
            this.currentSituation = currentSituation;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (dispatchedUniversity != null) {
            this.dispatchedUniversity = dispatchedUniversity;
        }
        if (dispatchedCountry != null) {
            this.dispatchedCountry = dispatchedCountry;
        }
        if (dispatchedRegion != null) {
            this.dispatchedRegion = dispatchedRegion;
        }
        if (domesticUniversity != null) {
            this.domesticUniversity = domesticUniversity;
        }
        if (dispatchYear != null) {
            this.dispatchYear = dispatchYear;
        }
        if (dispatchSemester != null) {
            this.dispatchSemester = dispatchSemester;
        }
        updateSituationDatesIfPresent(applicationDeadline, departureDate, dispatchStartDate, returnDate);
    }

    public void updateSituationDates(LocalDate applicationDeadline, LocalDate departureDate,
                                     LocalDate dispatchStartDate, LocalDate returnDate) {
        this.applicationDeadline = applicationDeadline;
        this.departureDate = departureDate;
        this.dispatchStartDate = dispatchStartDate;
        this.returnDate = returnDate;
    }

    public void updateSituationDatesIfPresent(LocalDate applicationDeadline, LocalDate departureDate,
                                              LocalDate dispatchStartDate, LocalDate returnDate) {
        if (applicationDeadline != null) {
            this.applicationDeadline = applicationDeadline;
        }
        if (departureDate != null) {
            this.departureDate = departureDate;
        }
        if (dispatchStartDate != null) {
            this.dispatchStartDate = dispatchStartDate;
        }
        if (returnDate != null) {
            this.returnDate = returnDate;
        }
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 지금까지 이 회원에게 발급된 Access Token을 모두 무효로 만든다 */
    public void invalidateIssuedTokens() {
        this.tokenVersion++;
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
