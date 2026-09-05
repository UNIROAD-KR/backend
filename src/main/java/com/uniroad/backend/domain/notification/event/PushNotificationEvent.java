package com.uniroad.backend.domain.notification.event;

import java.util.Map;

/**
 * "이 회원에게 푸시를 보내라"는 사실만 담는다.
 * 알림 행이 커밋된 뒤에 처리되므로 엔티티가 아니라 식별자와 값만 싣는다.
 */
public record PushNotificationEvent(
        Long memberId,
        String title,
        String body,
        Map<String, String> data,
        /** Android 알림 채널 ID */
        String channelId,
        /** 같은 대화의 알림을 하나로 덮어쓰기 위한 키 (예: chat-12) */
        String collapseKey
) {
}
