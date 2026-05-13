package com.uniroad.backend.domain.auth.service;

import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfo;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final RestTemplate restTemplate;

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    /**
     * 소셜 액세스 토큰으로 사용자 정보 조회
     */
    public OAuth2UserInfo getUserInfo(String provider, String accessToken) {
        if ("apple".equalsIgnoreCase(provider)) {
            // Apple은 id_token(JWT)을 accessToken 파라미터로 받아서 Payload를 디코딩하여 사용합니다.
            return OAuth2UserInfoFactory.of(provider, parseAppleToken(accessToken));
        }

        String url = switch (provider.toLowerCase()) {
            case "kakao" -> KAKAO_USER_INFO_URL;
            case "naver" -> NAVER_USER_INFO_URL;
            case "google" -> GOOGLE_USER_INFO_URL;
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

    private Map<String, Object> parseAppleToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
            }
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("[SocialAuth] Apple ID Token 파싱 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH2_TOKEN);
        }
    }
}
