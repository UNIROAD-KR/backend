package com.uniroad.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 등록 요청")
public record FcmTokenRequest(
        @NotBlank
        @Schema(description = "FCM registration token", example = "fcm-token-value")
        String token
) {
}
