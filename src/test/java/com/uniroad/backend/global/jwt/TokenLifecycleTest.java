package com.uniroad.backend.global.jwt;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Access Token의 수명 경계를 지키는 테스트.
 *
 * 여기서 검증하는 세 가지는 모두 실제로 뚫려 있던 구멍이다.
 * 회귀하면 로그아웃과 비밀번호 변경이 조용히 무력해지므로 테스트로 고정해 둔다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TokenLifecycleTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .username("token-lifecycle")
                .email("token-lifecycle@uniroad.test")
                .password("{noop}irrelevant")
                .name("토큰")
                .provider("LOCAL")
                .role(Role.USER)
                .status(MemberStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Refresh Token은 Access Token 자리에서 거부된다")
    void refreshTokenIsRejectedWhereAccessTokenIsExpected() {
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        assertThatThrownBy(() -> jwtProvider.parseAccessClaims(refreshToken))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN.name());
    }

    @Test
    @DisplayName("Access Token은 재발급 자리에서 거부된다")
    void accessTokenIsRejectedWhereRefreshTokenIsExpected() {
        String accessToken = jwtProvider.createAccessToken(
                member.getId(), member.getRole().getKey(), member.getTokenVersion());

        assertThatThrownBy(() -> jwtProvider.parseRefreshClaims(accessToken))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN.name());
    }

    @Test
    @DisplayName("토큰 세대가 올라가면 그 전에 발급된 Access Token은 즉시 무효가 된다")
    void bumpingTokenVersionRevokesAlreadyIssuedAccessTokens() {
        String accessToken = jwtProvider.createAccessToken(
                member.getId(), member.getRole().getKey(), member.getTokenVersion());
        var claims = jwtProvider.parseAccessClaims(accessToken);

        // 발급 직후에는 통과한다
        assertThatCode(() -> userDetailsService.loadUserById(
                jwtProvider.getMemberId(claims), jwtProvider.getTokenVersion(claims)))
                .doesNotThrowAnyException();

        // 로그아웃·비밀번호 변경이 하는 일
        member.invalidateIssuedTokens();
        memberRepository.saveAndFlush(member);

        assertThatThrownBy(() -> userDetailsService.loadUserById(
                jwtProvider.getMemberId(claims), jwtProvider.getTokenVersion(claims)))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getCode())
                .isEqualTo(ErrorCode.EXPIRED_TOKEN.name());
    }

    @Test
    @DisplayName("tv 클레임이 없는 예전 토큰은 최초 세대(0)로 간주해 배포 순간 튕기지 않는다")
    void legacyTokenWithoutVersionClaimIsTreatedAsGenerationZero() {
        String legacyToken = io.jsonwebtoken.Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim(JwtProvider.CLAIM_ROLE, member.getRole().getKey())
                .claim(JwtProvider.CLAIM_TYPE, "ACCESS")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        io.jsonwebtoken.io.Decoders.BASE64.decode(
                                "ZGV2LWdyZWVucGF0aC1zZWNyZXQta2V5LW11c3QtYmUtYXQtbGVhc3QtMjU2Yml0cw==")))
                .compact();

        assertThat(jwtProvider.getTokenVersion(jwtProvider.parseAccessClaims(legacyToken))).isZero();
    }
}
