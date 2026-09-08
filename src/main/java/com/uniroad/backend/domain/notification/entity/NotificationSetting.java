package com.uniroad.backend.domain.notification.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원별 푸시 알림 on/off. 앱 "알림 설정" 화면의 스위치와 1:1로 대응한다.
 *
 * 끈 알림도 알림함에는 그대로 쌓는다 — 푸시로 울리지만 않을 뿐,
 * 나중에 앱에서 알림함을 열면 놓친 내용을 볼 수 있어야 한다.
 *
 * 기기가 아니라 회원에 붙인다. 앱은 전체 알림을 끄면 그 기기의 FCM 토큰을 지우지만,
 * 그것만으로는 다른 기기에 그대로 푸시가 간다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_setting", indexes = {
        @Index(name = "uk_notification_setting_member", columnList = "member_id", unique = true)
})
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 전체 알림. 끄면 종류별 설정과 관계없이 어떤 푸시도 보내지 않는다.
     *
     * 아래 스위치들이 모두 DB 기본값을 들고 있는 이유:
     * ddl-auto=update는 이미 행이 있는 테이블에 not null 컬럼을 붙이지 못하고 경고만 남긴다.
     * 그러면 스위치를 하나 새로 추가할 때마다 배포 후 조회가 "컬럼 없음"으로 죽는다.
     * 기본값을 주면 ALTER가 그대로 통과한다.
     */
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean allEnabled = true;

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean chatEnabled = NotificationCategory.CHAT.isDefaultEnabled();

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean marketEnabled = NotificationCategory.MARKET.isDefaultEnabled();

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean communityEnabled = NotificationCategory.COMMUNITY.isDefaultEnabled();

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean scheduleEnabled = NotificationCategory.SCHEDULE.isDefaultEnabled();

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean marketingEnabled = NotificationCategory.MARKETING.isDefaultEnabled();

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean noticeEnabled = NotificationCategory.NOTICE.isDefaultEnabled();

    private NotificationSetting(Member member) {
        this.member = member;
    }

    public static NotificationSetting createDefault(Member member) {
        return new NotificationSetting(member);
    }

    /**
     * 앱이 화면의 스위치를 통째로 보내오지만, 일부만 담아 보내도 나머지는 건드리지 않는다.
     * null은 "이 항목은 그대로 둔다"는 뜻이다.
     */
    public void update(
            Boolean allEnabled,
            Boolean chatEnabled,
            Boolean marketEnabled,
            Boolean communityEnabled,
            Boolean scheduleEnabled,
            Boolean marketingEnabled,
            Boolean noticeEnabled
    ) {
        if (allEnabled != null) {
            this.allEnabled = allEnabled;
        }
        if (chatEnabled != null) {
            this.chatEnabled = chatEnabled;
        }
        if (marketEnabled != null) {
            this.marketEnabled = marketEnabled;
        }
        if (communityEnabled != null) {
            this.communityEnabled = communityEnabled;
        }
        if (scheduleEnabled != null) {
            this.scheduleEnabled = scheduleEnabled;
        }
        if (marketingEnabled != null) {
            this.marketingEnabled = marketingEnabled;
        }
        if (noticeEnabled != null) {
            this.noticeEnabled = noticeEnabled;
        }
    }

    /** 전체 알림 스위치 하나만 바꾼다 */
    public void updateAllEnabled(boolean enabled) {
        this.allEnabled = enabled;
    }

    /** 종류별 스위치 하나만 바꾼다 */
    public void updateCategory(NotificationCategory category, boolean enabled) {
        switch (category) {
            case CHAT -> this.chatEnabled = enabled;
            case MARKET -> this.marketEnabled = enabled;
            case COMMUNITY -> this.communityEnabled = enabled;
            case SCHEDULE -> this.scheduleEnabled = enabled;
            case MARKETING -> this.marketingEnabled = enabled;
            case NOTICE -> this.noticeEnabled = enabled;
        }
    }

    /** 이 종류의 알림을 푸시로 보내도 되는지. 알림함 행은 이 값과 무관하게 항상 만든다. */
    public boolean allowsPush(NotificationType type) {
        if (!allEnabled) {
            return false;
        }

        NotificationCategory category = type.category();
        if (category == null) {
            return true;
        }

        return isEnabled(category);
    }

    public boolean isEnabled(NotificationCategory category) {
        return switch (category) {
            case CHAT -> chatEnabled;
            case MARKET -> marketEnabled;
            case COMMUNITY -> communityEnabled;
            case SCHEDULE -> scheduleEnabled;
            case MARKETING -> marketingEnabled;
            case NOTICE -> noticeEnabled;
        };
    }
}
