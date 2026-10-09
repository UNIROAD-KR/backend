package com.uniroad.backend.domain.calendar.service;

import com.uniroad.backend.domain.calendar.entity.CalendarEvent;
import com.uniroad.backend.domain.calendar.entity.CalendarRepeatType;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 반복 일정을 조회 범위 안의 회차로 펼친다.
 *
 * 회차를 행으로 미리 만들어 두지 않는다. 끝없는 반복은 행으로 만들 수 없고,
 * 일정을 고칠 때마다 이미 만든 행을 전부 다시 맞춰야 한다.
 */
public final class CalendarOccurrenceExpander {

    private CalendarOccurrenceExpander() {
    }

    public record Occurrence(CalendarEvent event, long index, LocalDateTime startAt, LocalDateTime endAt) {
    }

    /** from과 to는 둘 다 포함한다. 반복하지 않는 일정은 범위와 겹치면 한 건을 돌려준다. */
    public static List<Occurrence> expand(CalendarEvent event, LocalDate from, LocalDate to) {
        CalendarRepeatType repeatType = event.getRepeatType();
        LocalDateTime first = event.getStartAt();
        Duration duration = Duration.between(first, event.getEndAt());
        LocalDate until = event.getRepeatUntil();

        List<Occurrence> occurrences = new ArrayList<>();

        if (!repeatType.repeats()) {
            if (!event.getStartDate().isAfter(to) && !event.getEndDate().isBefore(from)) {
                occurrences.add(new Occurrence(event, 0, first, event.getEndAt()));
            }
            return occurrences;
        }

        // 몇 년 전에 시작한 매일 반복을 첫 회차부터 세지 않도록, 범위 직전 회차로 건너뛴다.
        // 말일로 당겨지는 달 때문에 between이 한 회차 적게 셀 수 있어 하나 더 앞에서 시작한다.
        LocalDate earliestStartDate = from.minusDays(event.getSpanDays());
        long index = Math.max(0, repeatType.unit().between(first.toLocalDate(), earliestStartDate) - 1);

        for (; ; index++) {
            LocalDateTime startAt = repeatType.shift(first, index);
            LocalDate startDate = startAt.toLocalDate();
            if (startDate.isAfter(to) || (until != null && startDate.isAfter(until))) {
                break;
            }

            LocalDateTime endAt = startAt.plus(duration);
            if (!endAt.toLocalDate().isBefore(from)) {
                occurrences.add(new Occurrence(event, index, startAt, endAt));
            }
        }

        return occurrences;
    }
}
