package com.uniroad.backend.domain.calendar.entity;

/**
 * 일정 알림 시점.
 *
 * 서버는 값만 보관한다. 실제 알림은 앱이 기기 로컬 알림으로 예약한다 —
 * 일정 시각이 타임존 없는 현지 시각이라 서버는 "지금이 그 시각인지"를 알 수 없다.
 */
public enum CalendarReminder {
    NONE,
    AT_TIME,
    MIN_10,
    HOUR_1,
    DAY_1
}
