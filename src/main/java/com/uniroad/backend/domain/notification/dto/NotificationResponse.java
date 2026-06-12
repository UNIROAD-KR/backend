package com.uniroad.backend.domain.notification.dto;

import com.uniroad.backend.domain.notification.entity.Notification;
import com.uniroad.backend.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "알림 응답")
public class NotificationResponse {
    @Schema(description = "알림 ID", example = "1")
    private Long notificationId;

    @Schema(description = "알림 타입", example = "CHAT")
    private NotificationType type;

    @Schema(description = "알림 제목", example = "새 채팅 메시지")
    private String title;

    @Schema(description = "알림 내용", example = "새 메시지가 도착했습니다.")
    private String content;

    @Schema(description = "클릭 시 이동할 대상 ID. CHAT은 roomId입니다.", example = "12")
    private Long referenceId;

    @Schema(description = "채팅방 이동용 ID. CHAT 타입에서 referenceId와 동일합니다.", example = "12")
    private Long roomId;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        Long roomId = notification.getType() == NotificationType.CHAT ? notification.getReferenceId() : null;
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .referenceId(notification.getReferenceId())
                .roomId(roomId)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
