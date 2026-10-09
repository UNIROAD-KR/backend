package com.uniroad.backend.domain.calendar.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일정에 첨부한 사진.
 *
 * URL이 아니라 S3 key를 저장한다. 비공개 경로에 올라가므로 조회할 때마다 읽기용 URL을 새로 서명한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "calendar_event_photo", indexes = {
        @Index(name = "idx_calendar_event_photo_event_order", columnList = "event_id, sort_order"),
        @Index(name = "idx_calendar_event_photo_member", columnList = "member_id")
})
public class CalendarEventPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    /** 탈퇴할 때 일정을 거치지 않고 한 번에 지우기 위해 사진에도 회원을 둔다. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String imageKey;

    /** 앱이 축소본을 올리지 않았으면 null이고, 그때는 원본을 썸네일로 쓴다. */
    @Column(length = 500)
    private String thumbnailKey;

    /** 사용자가 고른 순서. 여러 날 일정에서는 시작일부터 하루에 한 장씩 이 순서로 배정한다. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    private Integer width;

    private Integer height;

    @Builder
    public CalendarEventPhoto(
            CalendarEvent event,
            Member member,
            String imageKey,
            String thumbnailKey,
            int sortOrder,
            Integer width,
            Integer height
    ) {
        this.event = event;
        this.member = member;
        this.imageKey = imageKey;
        this.thumbnailKey = thumbnailKey;
        this.sortOrder = sortOrder;
        this.width = width;
        this.height = height;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getThumbnailKeyOrImageKey() {
        return thumbnailKey != null ? thumbnailKey : imageKey;
    }
}
