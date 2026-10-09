package com.uniroad.backend.domain.calendar.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 캘린더 일정.
 *
 * startAt/endAt은 타임존이 없는 현지 시각이다. 교환학생은 파견국과 한국을 오가는데,
 * 절대 시각으로 저장하면 귀국한 뒤 "9월 21일 오전 9시"의 기록이 다른 날짜로 밀려 보인다.
 * 하루 종일 일정은 두 값 모두 그 날짜의 00:00으로 맞춘다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "calendar_event", indexes = {
        @Index(name = "idx_calendar_event_member_start", columnList = "member_id, start_at")
})
public class CalendarEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 카테고리가 지워지면 null(미분류)이 된다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CalendarCategory category;

    /** 사진만 올린 기록은 제목이 없다. */
    @Column(length = 100)
    private String title;

    @Column(nullable = false)
    private boolean allDay;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 값을 새로 추가해도 기존 DB에서 INSERT가 죽지 않도록 네이티브 ENUM 대신 varchar로 만든다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private CalendarReminder reminder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private CalendarRepeatType repeatType;

    /** 이 날짜에 시작하는 회차까지 반복한다. null이면 끝이 없다. */
    private LocalDate repeatUntil;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<CalendarEventPhoto> photos = new ArrayList<>();

    @Builder
    public CalendarEvent(
            Member member,
            CalendarCategory category,
            String title,
            boolean allDay,
            LocalDateTime startAt,
            LocalDateTime endAt,
            CalendarReminder reminder,
            CalendarRepeatType repeatType,
            LocalDate repeatUntil,
            String memo
    ) {
        this.member = member;
        update(category, title, allDay, startAt, endAt, reminder, repeatType, repeatUntil, memo);
    }

    public void update(
            CalendarCategory category,
            String title,
            boolean allDay,
            LocalDateTime startAt,
            LocalDateTime endAt,
            CalendarReminder reminder,
            CalendarRepeatType repeatType,
            LocalDate repeatUntil,
            String memo
    ) {
        this.category = category;
        this.title = title;
        this.allDay = allDay;
        this.startAt = startAt;
        this.endAt = endAt;
        this.reminder = reminder;
        this.repeatType = repeatType;
        this.repeatUntil = repeatUntil;
        this.memo = memo;
    }

    public LocalDate getStartDate() {
        return startAt.toLocalDate();
    }

    public LocalDate getEndDate() {
        return endAt.toLocalDate();
    }

    /** 시작일과 종료일 사이의 일수. 하루짜리 일정은 0이다. */
    public int getSpanDays() {
        return (int) ChronoUnit.DAYS.between(getStartDate(), getEndDate());
    }

    /** 반복 회차가 아닌, 일정을 등록한 원래 날짜 범위에 이 날짜가 들어가는지. */
    public boolean covers(LocalDate date) {
        return !date.isBefore(getStartDate()) && !date.isAfter(getEndDate());
    }

    /** 사진 목록을 통째로 바꾼다. 빠진 사진은 orphanRemoval로 행이 지워진다. */
    public void replacePhotos(List<CalendarEventPhoto> next) {
        // 넘겨받은 목록이 기존 사진을 담고 있을 수 있어 복사한 뒤에 비운다.
        List<CalendarEventPhoto> copy = List.copyOf(next);
        photos.clear();
        photos.addAll(copy);
    }
}
