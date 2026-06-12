package com.uniroad.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "읽지 않은 알림 개수 응답")
public record UnreadCountResponse(
        @Schema(description = "읽지 않은 알림 개수", example = "3")
        long count
) {
}
