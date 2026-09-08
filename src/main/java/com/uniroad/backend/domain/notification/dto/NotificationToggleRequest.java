package com.uniroad.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 스위치 하나를 켜고 끄는 요청")
public record NotificationToggleRequest(
        @NotNull(message = "enabled는 필수입니다.")
        @Schema(description = "켤지 끌지", example = "false")
        Boolean enabled
) {
}
