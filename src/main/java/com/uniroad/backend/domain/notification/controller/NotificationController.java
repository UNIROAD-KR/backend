package com.uniroad.backend.domain.notification.controller;

import com.uniroad.backend.domain.notification.dto.FcmTokenRequest;
import com.uniroad.backend.domain.notification.dto.FcmPushResponse;
import com.uniroad.backend.domain.notification.dto.FcmTestPushRequest;
import com.uniroad.backend.domain.notification.dto.NotificationResponse;
import com.uniroad.backend.domain.notification.dto.NotificationSettingRequest;
import com.uniroad.backend.domain.notification.dto.NotificationSettingResponse;
import com.uniroad.backend.domain.notification.dto.NotificationToggleRequest;
import com.uniroad.backend.domain.notification.entity.NotificationCategory;
import com.uniroad.backend.domain.notification.dto.UnreadCountResponse;
import com.uniroad.backend.domain.notification.service.FcmService;
import com.uniroad.backend.domain.notification.service.NotificationService;
import com.uniroad.backend.domain.notification.service.NotificationSettingService;
import com.uniroad.backend.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final NotificationSettingService notificationSettingService;
    private final FcmService fcmService;

    @Operation(
            summary = "알림 설정 조회",
            description = "앱 알림 설정 화면의 스위치 값을 돌려줍니다. 한 번도 저장한 적이 없으면 기본값을 돌려줍니다."
    )
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingResponse> getNotificationSettings() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationSettingService.get(memberId));
    }

    @Operation(
            summary = "알림 설정 변경",
            description = "앱 알림 설정 화면의 스위치를 저장합니다. 값을 빼고 보내면 그 항목은 서버에 저장된 값을 유지합니다. "
                    + "끈 알림은 FCM 푸시만 보내지 않습니다 - 알림함에는 그대로 쌓이므로 앱에서 알림함을 열면 확인할 수 있습니다. "
                    + "공지(NOTICE)와 시스템 안내(SYSTEM)는 종류별로 끌 수 없고, 전체 알림을 껐을 때만 막힙니다."
    )
    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingResponse> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationSettingService.update(memberId, request));
    }

    @Operation(
            summary = "전체 알림 끄기/켜기",
            description = "전체 알림 스위치 하나만 바꿉니다. 끄면 종류별 설정과 관계없이 어떤 푸시도 보내지 않습니다."
    )
    @PatchMapping("/settings/all")
    public ResponseEntity<NotificationSettingResponse> updateAllNotificationSetting(
            @Valid @RequestBody NotificationToggleRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationSettingService.updateAllEnabled(memberId, request.enabled()));
    }

    @Operation(
            summary = "알림 종류별 끄기/켜기",
            description = "종류 하나의 스위치만 바꿉니다. CHAT은 채팅, COMMUNITY는 내 글의 댓글, NOTICE는 공지사항입니다. "
                    + "끈 종류도 알림함에는 그대로 쌓이고 FCM 푸시만 나가지 않습니다. "
                    + "점검·보안 같은 필수 안내(SYSTEM)는 종류별로 끌 수 없고 전체 알림을 껐을 때만 막힙니다."
    )
    @PatchMapping("/settings/categories/{category}")
    public ResponseEntity<NotificationSettingResponse> updateNotificationCategorySetting(
            @PathVariable NotificationCategory category,
            @Valid @RequestBody NotificationToggleRequest request
    ) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(
                notificationSettingService.updateCategory(memberId, category, request.enabled()));
    }

    @Operation(
            summary = "읽지 않은 알림 목록 조회",
            description = "현재 로그인 사용자의 읽지 않은 알림을 최신순으로 페이징 조회합니다. CHAT 알림의 referenceId와 roomId는 채팅방 ID입니다."
    )
    @GetMapping({"/unread", ""})
    public ResponseEntity<Page<NotificationResponse>> getNotifications(@PageableDefault(size = 20) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(memberId, pageable));
    }

    @Operation(summary = "전체 알림 목록 조회", description = "현재 로그인 사용자의 전체 알림을 최신순으로 페이징 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(@PageableDefault(size = 20) Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(notificationService.getNotifications(memberId, pageable));
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

    @Operation(
            summary = "FCM 토큰 삭제",
            description = "로그아웃한 기기로 푸시가 계속 가지 않도록 해당 기기의 토큰을 지웁니다. "
                    + "다른 기기의 토큰은 그대로 두므로, 지울 토큰을 본문에 담아 보내세요."
    )
    @DeleteMapping("/fcm-token")
    public ResponseEntity<Void> deleteFcmToken(@Valid @RequestBody FcmTokenRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        fcmService.deleteToken(memberId, request.token());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "FCM test push send", description = "Sends a test push notification to a specific member. Admin only.")
    @PostMapping("/test-push/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FcmPushResponse> sendTestPush(
            @PathVariable Long memberId,
            @Valid @RequestBody FcmTestPushRequest request
    ) {
        FcmPushResponse response = fcmService.sendTestToMember(
                memberId,
                request.title(),
                request.body(),
                request.data()
        );
        return ResponseEntity.ok(response);
    }
}
