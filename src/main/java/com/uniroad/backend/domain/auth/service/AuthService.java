package com.uniroad.backend.domain.auth.service;

import com.uniroad.backend.domain.auth.dto.*;
import com.uniroad.backend.domain.auth.entity.RefreshToken;
import com.uniroad.backend.domain.auth.repository.RefreshTokenRepository;
import com.uniroad.backend.domain.info.entity.University;
import com.uniroad.backend.domain.info.repository.UniversityRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.jwt.JwtProvider;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 일반 로그인(JWT) 인증 서비스
 *
 * 담당 기능:
 * - 회원가입 (이메일 중복 검사 + 비밀번호 인코딩)
 * - 로그인 (비밀번호 검증 + Access/Refresh 토큰 발급)
 * - 토큰 재발급 (Refresh Token Rotation 전략)
 * - 로그아웃 (Refresh Token DB 삭제)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final SocialAuthService socialAuthService;
    private final UniversityRepository universityRepository;

    // ── 회원가입 ────────────────────────────────────────────────

    @Transactional
    public Long signUp(SignUpRequest request) {
        if (memberRepository.findByUsername(request.username()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        String email = (request.email() == null || request.email().isBlank()) ? null : request.email();
        if (email != null && memberRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .username(request.username())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .provider("LOCAL")
                .role(Role.USER)
                .status(com.uniroad.backend.domain.member.entity.MemberStatus.NEED_ONBOARDING)
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("[SignUp] 새 회원 가입: username={}, memberId={}", request.username(), savedMember.getId());
        return savedMember.getId();
    }

    @Transactional
    public void onboarding(Long memberId, OnboardingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        University domesticUniversity = findOrCreateUniversity(request.domesticUniversity());

        member.completeOnboarding(
                request.age(),
                domesticUniversity,
                request.dispatchedUniversity(),
                request.dispatchedCountry(),
                request.dispatchedRegion()
        );
    }

    private University findOrCreateUniversity(String universityName) {
        String normalizedName = normalizeRequired(universityName);
        return universityRepository.findByName(normalizedName)
                .orElseGet(() -> universityRepository.save(
                        University.builder()
                                .name(normalizedName)
                                .build()
                ));
    }

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return value.trim();
    }

    /**
     * 소셜 가입 회원 - 아이디 및 비밀번호 설정 (회원가입 완료)
     */
    @Transactional
    public void socialSignUp(Long memberId, SocialSignUpRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() != MemberStatus.NEED_SIGNUP) {
            throw new CustomException(ErrorCode.ALREADY_SIGNED_UP);
        }

        // 1. 아이디 중복 확인
        if (memberRepository.findByUsername(request.username()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 2. 이메일 중복 확인 (새로운 이메일이 입력된 경우에만)
        String email = (request.email() == null || request.email().isBlank()) ? null : request.email();
        if (email != null && !email.equals(member.getEmail())) {
            if (memberRepository.findByEmail(email).isPresent()) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
            member.updateEmail(email);
        }

        // 3. 아이디, 비밀번호 설정 및 상태 변경
        member.updateUsername(request.username());
        member.updatePassword(passwordEncoder.encode(request.password()));
        member.updateStatus(MemberStatus.NEED_ONBOARDING);

        log.info("[SocialSignUp] 소셜 회원 아이디/비밀번호 생성 완료: memberId={}, username={}", memberId, request.username());
    }

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public void checkEmailDuplication(String email) {
        if (email != null && !email.isBlank()) {
            if (memberRepository.findByEmail(email).isPresent()) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
        }
     }

    /**
     * 아이디 중복 확인
     */
    @Transactional(readOnly = true)
    public void checkUsernameDuplication(String username) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }
    }

    // ── 로그인 ──────────────────────────────────────────────────

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 1. 아이디로 회원 조회
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. 토큰 발급
        return issueTokens(member);
    }

    /**
     * 소셜 로그인 (App SDK 방식)
     */
    @Transactional
    public TokenResponse socialLogin(SocialLoginRequest request) {
        // 1. 소셜 프로바이더로부터 사용자 정보 조회
        OAuth2UserInfo userInfo = socialAuthService.getUserInfo(request.provider(), request.accessToken());

        // 2. DB 저장 또는 업데이트
        Member member = saveOrUpdateSocialMember(request.provider(), userInfo);

        // 3. 토큰 발급
        return issueTokens(member);
    }

    /**
     * 소셜 사용자 저장 또는 업데이트 (공통 로직)
     */
    @Transactional
    public Member saveOrUpdateSocialMember(String provider, OAuth2UserInfo userInfo) {
        return memberRepository.findByProviderAndProviderId(provider, userInfo.getId())
                .map(existing -> {
                    existing.updateName(userInfo.getName());
                    return existing;
                })
                .orElseGet(() -> {
                    // 동일 이메일로 일반 가입된 회원이 있는 경우 소셜 계정 연동
                    if (userInfo.getEmail() != null) {
                        return memberRepository.findByEmail(userInfo.getEmail())
                                .map(existing -> {
                                    existing.linkOAuth2(provider, userInfo.getId());
                                    return existing;
                                })
                                .orElseGet(() -> createSocialMember(provider, userInfo));
                    }
                    return createSocialMember(provider, userInfo);
                });
    }

    private Member createSocialMember(String provider, OAuth2UserInfo userInfo) {
        Member member = Member.builder()
                .email(userInfo.getEmail() != null
                        ? userInfo.getEmail()
                        : provider + "_" + userInfo.getId() + "@social.uniroad")
                .name(userInfo.getName() != null ? userInfo.getName() : "소셜회원")
                .provider(provider)
                .providerId(userInfo.getId())
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }

    // ── 토큰 재발급 (Token Rotation) ────────────────────────────

    /**
     * Refresh Token 검증 후 Access + Refresh Token 모두 재발급
     *
     * Token Rotation 전략:
     * - 재발급 시 기존 Refresh Token을 새 것으로 교체 (DB 업데이트)
     * - 탈취된 Refresh Token으로 재발급 시도 시 → DB의 토큰과 불일치 → 거부
     * - 탈취 의심 시 해당 회원의 모든 토큰을 무효화할 수 있음
     */
    @Transactional
    public TokenResponse reissue(ReissueRequest request, String clientIp) {
        String oldRefreshToken = request.refreshToken();

        // 1. JWT 서명/만료 검증
        if (!jwtProvider.validateToken(oldRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtProvider.getMemberId(oldRefreshToken);

        // 2. DB에서 Refresh Token 조회 및 일치 여부 확인
        RefreshToken savedToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!savedToken.getToken().equals(oldRefreshToken)) {
            // 토큰 불일치 → 탈취 의심 → 해당 회원 토큰 전체 삭제 (강제 재로그인 유도)
            refreshTokenRepository.deleteByMemberId(memberId);
            log.warn("[보안 경보] Refresh Token 불일치 - memberId={}, ip={}", memberId, clientIp);
            throw new CustomException(ErrorCode.TOKEN_MISMATCH);
        }

        if (savedToken.isExpired()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 3. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 4. 새 토큰 발급 + Refresh Token Rotation
        String newAccessToken  = jwtProvider.createAccessToken(memberId, member.getRole().getKey());
        String newRefreshToken = jwtProvider.createRefreshToken(memberId);
        Long ttl = jwtProvider.getRefreshTokenValiditySeconds();

        // Redis에서는 ID(token)가 변경되면 새 엔티티가 되므로 기존 것을 삭제하고 새로 저장
        refreshTokenRepository.delete(savedToken);
        
        RefreshToken newToken = RefreshToken.builder()
                .memberId(memberId)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(ttl))
                .lastUsedIp(clientIp)
                .createdAt(LocalDateTime.now())
                .ttl(ttl)
                .build();
        
        refreshTokenRepository.save(newToken);

        log.debug("[Token Reissued] memberId={}", memberId);
        return TokenResponse.of(newAccessToken, newRefreshToken, jwtProvider.getAccessTokenValiditySeconds(), member.getStatus());
    }

    // ── 로그아웃 ────────────────────────────────────────────────

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
        log.info("[Logout] memberId={}", memberId);
    }

    // ── 내부 공통 토큰 발급 메서드 ───────────────────────────────

    private TokenResponse issueTokens(Member member) {
        String accessToken  = jwtProvider.createAccessToken(member.getId(), member.getRole().getKey());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenValiditySeconds());

        // Refresh Token 저장 (기존 토큰 있으면 삭제 후 교체)
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresent(refreshTokenRepository::delete);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .memberId(member.getId())
                        .token(refreshToken)
                        .expiresAt(expiresAt)
                        .createdAt(LocalDateTime.now())
                        .ttl(jwtProvider.getRefreshTokenValiditySeconds())
                        .build()
        );

        return TokenResponse.of(accessToken, refreshToken, jwtProvider.getAccessTokenValiditySeconds(), member.getStatus());
    }
}
