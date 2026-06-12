package com.uniroad.backend.domain.notification.controller;

import com.uniroad.backend.domain.notification.dto.FcmTokenRequest;
import com.uniroad.backend.domain.notification.dto.NotificationResponse;
import com.uniroad.backend.domain.notification.dto.UnreadCountResponse;
import com.uniroad.backend.domain.notification.service.FcmService;
import com.uniroad.backend.domain.notification.service.NotificationService;
import com.uniroad.backend.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 조회, 읽음 처리, 삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/notifications", "/notifications"})
public class NotificationController {
    private final NotificationService notificationService;
    private final FcmService fcmService;

    @Operation(
            summary = "읽지 않은 알림 목록 조회",
            description = "현재 로그인 사용자의 읽지 않은 알림을 최신순으로 페이징 조회합니다. CHAT 알림의 referenceId와 roomId는 채팅방 ID입니다."
    )
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(@PageableDefault(size = 20) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(memberId, pageable));
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "프론트 상단 알림 아이콘 뱃지 표시용 개수를 반환합니다. 예: {\"count\":3}")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationService.getUnreadCount(memberId));
    }

    @Operation(summary = "특정 알림 읽음 처리", description = "현재 로그인 사용자의 특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationService.markAsRead(memberId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "현재 로그인 사용자의 모든 읽지 않은 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAllNotifications() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 알림 삭제", description = "현재 로그인 사용자의 특정 알림을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationService.delete(memberId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 알림 삭제", description = "현재 로그인 사용자의 모든 알림을 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        notificationService.deleteAll(memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "FCM 토큰 등록", description = "채팅 알림 푸시 전송을 위해 현재 로그인 사용자의 FCM registration token을 저장합니다.")
    @PatchMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(@Valid @RequestBody FcmTokenRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        fcmService.registerToken(memberId, request.token());
        return ResponseEntity.noContent().build();
    }
}
