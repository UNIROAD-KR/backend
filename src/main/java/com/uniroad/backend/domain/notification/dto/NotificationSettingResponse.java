package com.uniroad.backend.domain.notification.dto;

import com.uniroad.backend.domain.notification.entity.NotificationCategory;
import com.uniroad.backend.domain.notification.entity.NotificationSetting;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 응답")
public record NotificationSettingResponse(
        @Schema(description = "전체 알림", example = "true")
        boolean allEnabled,

        @Schema(description = "채팅 알림 (거래·동행 채팅 메시지)", example = "true")
        boolean chat,

        @Schema(description = "중고마켓 알림", example = "true")
        boolean market,

        @Schema(description = "커뮤니티 알림 (내 글의 댓글)", example = "true")
        boolean community,

        @Schema(description = "출국 준비 알림", example = "true")
        boolean schedule,

        @Schema(description = "혜택 및 이벤트", example = "false")
        boolean marketing,

        @Schema(description = "공지사항 알림. 꺼도 알림함에는 쌓이며, 필수 안내(SYSTEM)는 이 설정과 무관하게 발송됩니다.", example = "true")
        boolean notice
) {
    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(
                setting.isAllEnabled(),
                setting.isEnabled(NotificationCategory.CHAT),
                setting.isEnabled(NotificationCategory.MARKET),
                setting.isEnabled(NotificationCategory.COMMUNITY),
                setting.isEnabled(NotificationCategory.SCHEDULE),
                setting.isEnabled(NotificationCategory.MARKETING),
                setting.isEnabled(NotificationCategory.NOTICE)
        );
    }

    /** 아직 한 번도 저장한 적 없는 회원에게 돌려줄 값 */
    public static NotificationSettingResponse defaults() {
        return new NotificationSettingResponse(
                true,
                NotificationCategory.CHAT.isDefaultEnabled(),
                NotificationCategory.MARKET.isDefaultEnabled(),
                NotificationCategory.COMMUNITY.isDefaultEnabled(),
                NotificationCategory.SCHEDULE.isDefaultEnabled(),
                NotificationCategory.MARKETING.isDefaultEnabled(),
                NotificationCategory.NOTICE.isDefaultEnabled()
        );
    }
}
