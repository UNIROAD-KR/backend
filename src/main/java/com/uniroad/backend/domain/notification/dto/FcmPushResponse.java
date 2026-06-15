package com.uniroad.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM push send result")
public record FcmPushResponse(
        @Schema(description = "Target member ID", example = "1")
        Long targetMemberId,

        @Schema(description = "Number of registered FCM tokens for the target member", example = "2")
        int tokenCount,

        @Schema(description = "Number of successfully sent pushes", example = "2")
        int successCount,

        @Schema(description = "Whether FirebaseMessaging bean was available", example = "true")
        boolean firebaseAvailable
) {
}
