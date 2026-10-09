package com.uniroad.backend.domain.calendar.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public enum CalendarRepeatType {
    NONE(null, 0),
    DAILY(ChronoUnit.DAYS, 1),
    WEEKLY(ChronoUnit.WEEKS, 7),
    // 가장 짧은 달과 해를 기준으로 잡는다.
    MONTHLY(ChronoUnit.MONTHS, 28),
    YEARLY(ChronoUnit.YEARS, 365);

    private final ChronoUnit unit;
    private final int minIntervalDays;

    CalendarRepeatType(ChronoUnit unit, int minIntervalDays) {
        this.unit = unit;
        this.minIntervalDays = minIntervalDays;
    }

    public boolean repeats() {
        return this != NONE;
    }

    public ChronoUnit unit() {
        return unit;
    }

    /** 반복 간격의 최소 일수. 일정 길이가 이보다 길면 회차끼리 겹친다. */
    public int minIntervalDays() {
        return minIntervalDays;
    }

    /**
     * 첫 회차의 시각에서 index번째 회차의 시각을 구한다.
     *
     * 직전 회차가 아니라 항상 첫 회차에서 더한다. 31일에 시작한 매월 반복을 차례로 더하면
     * 2월에 28일로 당겨진 뒤 다시 31일로 돌아오지 못한다.
     * 그 달에 없는 날짜는 java.time이 말일로 당긴다.
     */
    public LocalDateTime shift(LocalDateTime first, long index) {
        return unit == null ? first : first.plus(index, unit);
    }
}
