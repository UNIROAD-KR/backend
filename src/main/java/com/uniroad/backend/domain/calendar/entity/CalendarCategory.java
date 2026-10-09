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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원이 직접 만드는 캘린더 카테고리. 색은 카테고리가 정하고 일정은 그 색을 따른다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "calendar_category", uniqueConstraints = {
        @UniqueConstraint(name = "uk_calendar_category_member_name", columnNames = {"member_id", "name"})
})
public class CalendarCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 20)
    private String name;

    /** #RRGGBB */
    @Column(nullable = false, length = 7)
    private String color;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public CalendarCategory(Member member, String name, String color, int sortOrder) {
        this.member = member;
        this.name = name;
        this.color = color;
        this.sortOrder = sortOrder;
    }

    /** null은 "이 항목은 그대로 둔다"는 뜻이다. */
    public void update(String name, String color) {
        if (name != null) {
            this.name = name;
        }
        if (color != null) {
            this.color = color;
        }
    }
}
