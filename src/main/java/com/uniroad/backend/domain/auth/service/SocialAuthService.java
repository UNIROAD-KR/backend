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

    /**
     * 소셜 액세스 토큰으로 사용자 정보 조회
     */
    public OAuth2UserInfo getUserInfo(String provider, String accessToken) {
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
}
