package com.uniroad.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 앱 알림 설정 화면의 스위치. 필드 이름은 앱이 기기에 저장하는 JSON과 같게 맞췄으므로
 * 화면에서 들고 있는 객체를 그대로 보내면 된다.
 *
 * 값을 빼고 보내면(null) 그 항목은 서버에 저장된 값을 그대로 둔다.
 */
@Schema(description = "알림 설정 변경 요청")
public record NotificationSettingRequest(
        @Schema(description = "전체 알림", example = "true")
        Boolean allEnabled,

        @Schema(description = "채팅 알림 (거래·동행 채팅 메시지)", example = "true")
        Boolean chat,

        @Schema(description = "중고마켓 알림", example = "true")
        Boolean market,

        @Schema(description = "커뮤니티 알림 (내 글의 댓글)", example = "true")
        Boolean community,

        @Schema(description = "출국 준비 알림", example = "true")
        Boolean schedule,

        @Schema(description = "혜택 및 이벤트", example = "false")
        Boolean marketing,

        @Schema(description = "공지사항 알림", example = "true")
        Boolean notice
) {
}
