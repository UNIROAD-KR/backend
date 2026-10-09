package com.uniroad.backend.domain.calendar;

import com.uniroad.backend.domain.calendar.entity.CalendarEvent;
import com.uniroad.backend.domain.calendar.entity.CalendarReminder;
import com.uniroad.backend.domain.calendar.entity.CalendarRepeatType;
import com.uniroad.backend.domain.calendar.service.CalendarOccurrenceExpander;
import com.uniroad.backend.domain.calendar.service.CalendarOccurrenceExpander.Occurrence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarOccurrenceExpanderTest {

    @Test
    @DisplayName("반복하지 않는 일정은 범위와 하루라도 겹칠 때만 한 건 나온다")
    void single() {
        CalendarEvent trip = event("2026-09-21T09:00", "2026-09-23T18:00", CalendarRepeatType.NONE, null);

        assertThat(startDates(trip, "2026-09-23", "2026-09-30")).containsExactly("2026-09-21");
        assertThat(startDates(trip, "2026-09-01", "2026-09-21")).containsExactly("2026-09-21");
        assertThat(startDates(trip, "2026-09-24", "2026-09-30")).isEmpty();
        assertThat(startDates(trip, "2026-09-01", "2026-09-20")).isEmpty();
    }

    @Test
    @DisplayName("31일에 시작한 매월 반복은 짧은 달에 말일로 당겨졌다가 다시 31일로 돌아온다")
    void monthlyClampsToMonthEnd() {
        CalendarEvent rent = event("2026-01-31T10:00", "2026-01-31T11:00", CalendarRepeatType.MONTHLY, null);

        assertThat(startDates(rent, "2026-02-01", "2026-05-31"))
                .containsExactly("2026-02-28", "2026-03-31", "2026-04-30", "2026-05-31");
    }

    @Test
    @DisplayName("오래전에 시작한 매일 반복도 범위 안의 날짜만, 원래 시각 그대로 나온다")
    void dailyStartedLongAgo() {
        CalendarEvent run = event("2020-01-01T07:00", "2020-01-01T07:30", CalendarRepeatType.DAILY, null);

        List<Occurrence> occurrences = CalendarOccurrenceExpander.expand(
                run, LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-30"));

        assertThat(occurrences).hasSize(30);
        assertThat(occurrences.get(0).startAt()).isEqualTo(LocalDateTime.parse("2026-09-01T07:00"));
        assertThat(occurrences.get(29).endAt()).isEqualTo(LocalDateTime.parse("2026-09-30T07:30"));
    }

    @Test
    @DisplayName("반복 종료일 이후에 시작하는 회차는 나오지 않는다")
    void repeatUntil() {
        CalendarEvent lecture = event("2026-09-07T13:00", "2026-09-07T15:00",
                CalendarRepeatType.WEEKLY, LocalDate.parse("2026-09-21"));

        assertThat(startDates(lecture, "2026-09-01", "2026-10-31"))
                .containsExactly("2026-09-07", "2026-09-14", "2026-09-21");
    }

    @Test
    @DisplayName("범위 전에 시작해 범위 안에서 끝나는 회차도 나온다")
    void occurrenceSpillingIntoRange() {
        // 금요일부터 일요일까지, 매주
        CalendarEvent weekend = event("2026-09-04T00:00", "2026-09-06T00:00", CalendarRepeatType.WEEKLY, null);

        assertThat(startDates(weekend, "2026-09-13", "2026-09-17")).containsExactly("2026-09-11");
    }

    @Test
    @DisplayName("2월 29일에 시작한 매년 반복은 평년에 28일로 당겨진다")
    void yearlyLeapDay() {
        CalendarEvent birthday = event("2024-02-29T00:00", "2024-02-29T00:00", CalendarRepeatType.YEARLY, null);

        assertThat(startDates(birthday, "2026-02-01", "2026-03-31")).containsExactly("2026-02-28");
        assertThat(startDates(birthday, "2028-02-01", "2028-03-31")).containsExactly("2028-02-29");
    }

    private List<String> startDates(CalendarEvent event, String from, String to) {
        return CalendarOccurrenceExpander.expand(event, LocalDate.parse(from), LocalDate.parse(to)).stream()
                .map(occurrence -> occurrence.startAt().toLocalDate().toString())
                .toList();
    }

    private CalendarEvent event(String startAt, String endAt, CalendarRepeatType repeatType, LocalDate repeatUntil) {
        return CalendarEvent.builder()
                .title("일정")
                .startAt(LocalDateTime.parse(startAt))
                .endAt(LocalDateTime.parse(endAt))
                .reminder(CalendarReminder.NONE)
                .repeatType(repeatType)
                .repeatUntil(repeatUntil)
                .build();
    }
}
