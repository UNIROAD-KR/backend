package com.uniroad.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 요청 (App SDK 전용)")
public record SocialLoginRequest(
    @Schema(description = "소셜 프로바이더 (kakao, naver, google, apple)", example = "kakao")
    @NotBlank(message = "프로바이더는 필수입니다.")
    String provider,

    @Schema(description = "소셜 액세스 토큰 (Google/Apple의 경우 ID Token)", example = "access_token_or_id_token")
    @NotBlank(message = "액세스 토큰은 필수입니다.")
    String accessToken
) {
}
