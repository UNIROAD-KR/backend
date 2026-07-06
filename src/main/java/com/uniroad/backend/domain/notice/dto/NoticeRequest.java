package com.uniroad.backend.domain.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "공지사항 생성/수정 요청")
public record NoticeRequest(
        @Schema(description = "제목", example = "서비스 점검 안내")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Schema(description = "내용", example = "7월 10일 새벽 2시부터 4시까지 점검이 진행됩니다.")
        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
}
