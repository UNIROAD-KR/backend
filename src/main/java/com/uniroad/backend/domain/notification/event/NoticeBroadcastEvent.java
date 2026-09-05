package com.uniroad.backend.domain.notification.event;

import java.util.Map;

/**
 * 공지처럼 전 회원에게 같은 내용을 보낼 때 쓴다.
 *
 * 회원마다 이벤트를 하나씩 띄우면 회원 수만큼 비동기 작업과 FCM 호출이 생긴다.
 * 내용이 모두 같으므로 한 번만 띄우고, 발송 쪽에서 토큰을 500개씩 묶어 보낸다.
 */
public record NoticeBroadcastEvent(
        String title,
        String body,
        Map<String, String> data,
        String channelId,
        String collapseKey
) {
}
