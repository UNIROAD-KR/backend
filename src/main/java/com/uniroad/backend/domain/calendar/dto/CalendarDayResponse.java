package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "선택한 날짜의 상세 응답")
public record CalendarDayResponse(
        LocalDate date,
        @Schema(description = "이 날짜의 대표 사진 ID. 사진이 없는 날은 null.")
        Long coverPhotoId,
        @Schema(description = "하루 종일 일정이 먼저, 그다음 시작 시각 순")
        List<CalendarEventResponse> events
) {
}
