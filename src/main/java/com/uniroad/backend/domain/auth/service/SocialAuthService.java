package com.uniroad.backend.domain.auth.service;

import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfo;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final RestTemplate restTemplate;

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";
    private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final NimbusJwtDecoder appleJwtDecoder = createAppleJwtDecoder();

    @Value("${social.apple.audiences:${APPLE_CLIENT_ID:}}")
    private String appleAudiences;

    /**
     * 소셜 액세스 토큰(또는 ID Token)으로 사용자 정보 조회
     */
    public OAuth2UserInfo getUserInfo(String provider, String accessToken) {
        if ("google".equalsIgnoreCase(provider)) {
            return getGoogleUserInfo(accessToken);
        }
        if ("apple".equalsIgnoreCase(provider)) {
            return getAppleUserInfo(accessToken);
        }

        String url = switch (provider.toLowerCase()) {
            case "kakao" -> KAKAO_USER_INFO_URL;
            case "naver" -> NAVER_USER_INFO_URL;
            default -> throw new CustomException(ErrorCode.OAUTH2_PROVIDER_NOT_SUPPORTED);
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> attributes = response.getBody();
            if (attributes == null) {
                throw new CustomException(ErrorCode.OAUTH2_USER_INFO_EMPTY);
            }

            return OAuth2UserInfoFactory.of(provider, attributes);
        } catch (Exception e) {
            log.error("[SocialAuth] 사용자 정보 조회 실패: provider={}, error={}", provider, e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        }
    }

    /**
     * 구글 ID Token 검증 및 사용자 정보 조회
     */
    private OAuth2UserInfo getGoogleUserInfo(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> attributes = response.getBody();
            if (attributes == null) {
                throw new CustomException(ErrorCode.OAUTH2_USER_INFO_EMPTY);
            }

            return OAuth2UserInfoFactory.of("google", attributes);
        } catch (Exception e) {
            log.error("[SocialAuth] 구글 ID Token 검증 실패: error={}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        }
    }

    /**
     * Apple identityToken 검증 및 사용자 정보 조회
     */
    private OAuth2UserInfo getAppleUserInfo(String identityToken) {
        try {
            Jwt jwt = appleJwtDecoder.decode(identityToken);
            validateAppleAudience(jwt);

            Map<String, Object> attributes = new HashMap<>(jwt.getClaims());
            return OAuth2UserInfoFactory.of("apple", attributes);
        } catch (JwtException | CustomException e) {
            log.error("[SocialAuth] Apple identityToken 검증 실패: error={}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        } catch (Exception e) {
            log.error("[SocialAuth] Apple 사용자 정보 조회 실패: error={}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        }
    }

    private void validateAppleAudience(Jwt jwt) {
        List<String> configuredAudiences = parseAppleAudiences();
        if (configuredAudiences.isEmpty()) {
            log.error("[SocialAuth] Apple audience 설정이 비어 있습니다. social.apple.audiences 또는 APPLE_CLIENT_ID를 설정하세요.");
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        }

        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
            boolean matches = token.getAudience().stream().anyMatch(configuredAudiences::contains);
            if (matches) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "Apple identityToken audience is invalid.",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        };

        OAuth2TokenValidatorResult result = audienceValidator.validate(jwt);
        if (result.hasErrors()) {
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        }
    }

    private List<String> parseAppleAudiences() {
        if (appleAudiences == null || appleAudiences.isBlank()) {
            return List.of();
        }
        return Arrays.stream(appleAudiences.split(","))
                .map(String::trim)
                .filter(audience -> !audience.isBlank())
                .toList();
    }

    private NimbusJwtDecoder createAppleJwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(APPLE_JWK_SET_URI).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(APPLE_ISSUER));
        return jwtDecoder;
    }
}
