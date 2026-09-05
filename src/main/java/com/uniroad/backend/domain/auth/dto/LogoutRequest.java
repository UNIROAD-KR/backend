package com.uniroad.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 요청")
public record LogoutRequest(
        @Schema(
                description = "이 기기의 FCM 토큰. 함께 보내면 로그아웃과 동시에 지워져 "
                        + "이후 이 기기로 알림이 가지 않습니다. 웹처럼 푸시를 쓰지 않으면 생략합니다.",
                example = "fcm-token-value"
        )
        String fcmToken
) {
}
