package com.uniroad.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "FCM test push request")
public record FcmTestPushRequest(
        @NotBlank
        @Size(max = 100)
        @Schema(description = "Push title", example = "Test notification")
        String title,

        @NotBlank
        @Size(max = 500)
        @Schema(description = "Push body", example = "This is a test push notification.")
        String body,

        @Schema(description = "Additional FCM data payload")
        Map<String, String> data
) {
}
