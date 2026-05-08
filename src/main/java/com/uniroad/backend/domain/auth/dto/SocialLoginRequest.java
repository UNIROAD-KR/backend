package com.uniroad.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 요청 (App SDK 전용)")
public record SocialLoginRequest(
    @Schema(description = "소셜 프로바이더 (kakao, naver)", example = "kakao")
    @NotBlank(message = "프로바이더는 필수입니다.")
    String provider,

    @Schema(description = "소셜 액세스 토큰 (SDK에서 획득한 것)", example = "access_token_from_sdk")
    @NotBlank(message = "액세스 토큰은 필수입니다.")
    String accessToken
) {
}
