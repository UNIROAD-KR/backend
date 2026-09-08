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
     * 끄게 두는 편이 낫다고 보고 항목을 만들었다. 안드로이드는 어차피 OS 설정에서
     * notice 채널을 끌 수 있어 막아도 막히지 않고, iOS는 종류별 제어가 없어
     * 항목이 없으면 공지가 성가신 사용자의 유일한 선택지가 "전체 알림 끄기"가 된다.
     * 공지 하나 때문에 채팅 알림까지 잃는 편이 더 나쁘다.
     *
     * 꺼도 알림함에는 그대로 쌓이므로 앱을 열면 공지를 확인할 수 있다.
     * 반드시 닿아야 하는 안내는 NOTICE가 아니라 SYSTEM으로 보낸다 — 그쪽은 끌 수 없다.
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
