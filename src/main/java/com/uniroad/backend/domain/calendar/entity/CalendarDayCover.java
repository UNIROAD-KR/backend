package com.uniroad.backend.domain.calendar.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자가 직접 고른 날짜별 대표 사진.
 *
 * 대표 사진은 기본적으로 조회할 때 계산하고, 사용자가 바꾼 날짜에만 행이 생긴다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "calendar_day_cover", uniqueConstraints = {
        @UniqueConstraint(name = "uk_calendar_day_cover_member_date", columnNames = {"member_id", "cover_date"})
})
public class CalendarDayCover extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "cover_date", nullable = false)
    private LocalDate coverDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private CalendarEventPhoto photo;

    private CalendarDayCover(Member member, LocalDate coverDate, CalendarEventPhoto photo) {
        this.member = member;
        this.coverDate = coverDate;
        this.photo = photo;
    }

    public static CalendarDayCover create(Member member, LocalDate coverDate, CalendarEventPhoto photo) {
        return new CalendarDayCover(member, coverDate, photo);
    }

    public void changePhoto(CalendarEventPhoto photo) {
        this.photo = photo;
    }
}
