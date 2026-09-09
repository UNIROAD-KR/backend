package com.uniroad.backend.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 / 검증 / 파싱 전담 컴포넌트
 *
 * Access  Token : 짧은 수명 (기본 30분)  → Authorization 헤더로 전달
 * Refresh Token : 긴 수명   (기본 14일)  → refresh_token 테이블에 저장하고 Body로 전달
 *
 * 두 토큰은 같은 키로 서명되므로 서명 검증만으로는 서로를 구분하지 못한다.
 * 예전에는 그래서 Refresh Token을 Authorization 헤더에 넣어도 인증이 통과했고,
 * 30분짜리로 설계한 자격 증명이 실제로는 14일을 살았다.
 * 지금은 파싱 진입점을 타입별로 나눠, 쓰려는 자리에 맞는 토큰만 통과시킨다.
 */
@Slf4j
@Component
public class JwtProvider {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLE = "role";
    /** 발급 시점의 Member.tokenVersion. 로그아웃·비밀번호 변경으로 값이 오르면 이 토큰은 무효가 된다. */
    public static final String CLAIM_TOKEN_VERSION = "tv";

    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
            @Value("${jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenValidityMs  = accessTokenValiditySeconds  * 1000L;
        this.refreshTokenValidityMs = refreshTokenValiditySeconds * 1000L;
    }

    // ── 토큰 생성 ──────────────────────────────────────────────

    /**
     * Access Token 생성
     * @param memberId      회원 PK (subject)
     * @param role          권한 문자열 (ex. "ROLE_USER")
     * @param tokenVersion  발급 시점의 Member.tokenVersion
     */
    public String createAccessToken(Long memberId, String role, int tokenVersion) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token 생성 (최소 정보만 포함 → DB가 진짜 저장소)
     */
    public String createRefreshToken(Long memberId) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMs);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // ── 토큰 파싱 / 검증 ──────────────────────────────────────

    /**
     * Access Token으로 쓰이는 자리의 유일한 파싱 진입점.
     * Refresh Token을 넣으면 여기서 막힌다.
     */
    public Claims parseAccessClaims(String token) {
        return parseTypedClaims(token, TYPE_ACCESS);
    }

    /**
     * 재발급 자리의 파싱 진입점. Access Token을 넣으면 여기서 막힌다.
     */
    public Claims parseRefreshClaims(String token) {
        return parseTypedClaims(token, TYPE_REFRESH);
    }

    private Claims parseTypedClaims(String token, String expectedType) {
        Claims claims = parseClaims(token);
        if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    /**
     * 서명과 만료만 검증한다. 타입을 구분하지 않으므로 바깥에 열지 않는다.
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Claims에서 회원 ID 추출
     */
    public Long getMemberId(Claims claims) {
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * Claims에서 발급 시점의 tokenVersion 추출.
     *
     * 이 클레임이 도입되기 전에 발급된 토큰에는 값이 없다. 그런 토큰까지 한 번에 막으면
     * 배포 순간 접속 중인 사용자가 전부 튕기므로, 당분간은 최초 버전(0)으로 간주한다.
     * 기존 토큰의 최대 수명(30분)이 지나면 이 관용은 없애고 null을 거부해도 된다.
     */
    public int getTokenVersion(Claims claims) {
        Integer tokenVersion = claims.get(CLAIM_TOKEN_VERSION, Integer.class);
        return tokenVersion == null ? 0 : tokenVersion;
    }

    /**
     * Access Token 만료까지 남은 시간(ms) 반환
     */
    public long getExpiration(Claims claims) {
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidityMs / 1000;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValidityMs / 1000;
    }
}
