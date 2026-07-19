package com.uniroad.backend.domain.notification.dto;

import com.uniroad.backend.domain.notification.entity.NotificationType;
import lombok.Builder;

@Builder
public record NotificationCreateRequest(
        NotificationType type,
        String title,
        String content,
        Long referenceId
) {
}
