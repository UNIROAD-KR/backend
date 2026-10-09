package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "대표 사진 변경 요청")
public record CalendarDayCoverRequest(
        @Schema(description = "그 날짜에 걸친 일정의 사진 ID", example = "88")
        @NotNull(message = "사진 ID는 필수입니다.")
        Long photoId
) {
}
