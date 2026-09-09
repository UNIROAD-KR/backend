package com.uniroad.backend.domain.notification.entity;

import java.util.Locale;

public enum NotificationType {
    CHAT,
    COMMENT,
    MATCH,
    LIKE,
    NOTICE,
    /** 교환학생 인증 심사 결과 */
    VERIFICATION,
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

    /**
     * 앱 "알림 설정" 화면에서 이 알림을 끄고 켜는 스위치.
     *
     * null이면 끌 수 없는 알림이다. 공지와 시스템 안내는 서비스 필수 안내라 종류별로 끄지
     * 못하게 두고, 전체 알림을 껐을 때만 막힌다.
     */
    public NotificationCategory category() {
        return switch (this) {
            // 동행 매칭은 결국 채팅으로 이어지므로 채팅 스위치를 따른다
            case CHAT, MATCH -> NotificationCategory.CHAT;
            // 앱 화면의 "커뮤니티 알림" 설명이 "내 글의 댓글과 관심 게시판 소식"이다
            case COMMENT, LIKE -> NotificationCategory.COMMUNITY;
            case NOTICE -> NotificationCategory.NOTICE;
            // 점검·보안·인증 결과처럼 반드시 닿아야 하는 안내다. 전체 알림을 껐을 때만 막힌다.
            case VERIFICATION, SYSTEM -> null;
        };
    }
}
