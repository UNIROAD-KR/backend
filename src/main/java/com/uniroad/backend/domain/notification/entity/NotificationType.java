package com.uniroad.backend.domain.notification.entity;

import java.util.Locale;

public enum NotificationType {
    CHAT,
    MATCH,
    LIKE,
    NOTICE,
    SYSTEM;

    /**
     * Android 알림 채널 ID.
     *
     * Android 8부터는 채널이 소리·헤드업 표시·진동을 결정하고, 사용자는 채널 단위로
     * 알림을 끈다. 서버가 채널을 지정하지 않으면 SDK 기본 채널로 떨어져
     * 앱이 만든 채널 설정이 통째로 무시된다.
     *
     * 여기서 정한 이름("chat", "notice", ...)과 똑같은 채널을 앱에서도 만들어야 한다.
     * 앱에 없는 채널을 보내도 FCM이 기본 채널로 되돌리므로 지금 넣어도 안전하다.
     */
    public String channelId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
