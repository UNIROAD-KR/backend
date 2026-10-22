package com.uniroad.backend.domain.notification.entity;

/**
 * 앱 "알림 설정" 화면의 스위치 한 칸.
 *
 * 이름과 기본값은 앱 화면(app/(tabs)/home/profile-notifications.tsx)의 항목과 그대로 맞춘다.
 * 여기서 끈 것은 FCM 푸시만 나가지 않고, 알림함 행은 그대로 쌓인다.
 */
public enum NotificationCategory {
    /** 채팅 알림 — 거래와 동행 채팅 메시지 */
    CHAT(true),

    /** 중고마켓 알림 — 아직 서버가 만드는 알림이 없다. 앱 화면에 항목이 있어 값만 보관한다. */
    MARKET(true),

    /** 커뮤니티 알림 — 내 글에 달린 댓글 */
    COMMUNITY(true),

    /** 출국 준비 알림 — 아직 서버가 만드는 알림이 없다. 앱 화면에 항목이 있어 값만 보관한다. */
    SCHEDULE(true),

    /** 혜택 및 이벤트 — 선택 수신이라 앱과 같이 꺼진 채로 시작한다 */
    MARKETING(false),

    /**
     * 공지사항.
     *
     * 공지는 지금 푸시(FCM)를 보내지 않고 알림함에만 쌓는다. 앱 설정 화면에 항목이 있어
     * 값은 계속 받아 보관한다 — 나중에 공지 푸시를 다시 켜면 이 값을 그대로 따르면 된다.
     * 반드시 기기에 닿아야 하는 안내는 NOTICE가 아니라 SYSTEM으로 보낸다.
     */
    NOTICE(true);

    private final boolean defaultEnabled;

    NotificationCategory(boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    /** 설정을 한 번도 저장하지 않은 회원에게 적용할 값 */
    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
}
