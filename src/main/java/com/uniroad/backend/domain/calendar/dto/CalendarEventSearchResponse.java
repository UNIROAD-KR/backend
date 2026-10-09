package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "일정 검색 결과 한 건")
public record CalendarEventSearchResponse(
        Long id,
        String title,
        String memo,
        Long categoryId,
        String categoryName,
        String color,
        boolean allDay,
        LocalDateTime startAt,
        LocalDateTime endAt,
        @Schema(description = "첫 번째 사진의 썸네일. 사진이 없으면 null.")
        String thumbnailUrl
) {
}
