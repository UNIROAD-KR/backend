package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "캘린더 메인 응답. 사진 토글이 켜져 있으면 dayCovers를, 꺼져 있으면 events의 제목을 셀에 그린다.")
public record CalendarMonthResponse(
        @Schema(description = "조회 범위와 겹치는 일정. 반복 일정은 회차마다 한 건씩 들어 있다.")
        List<Occurrence> events,
        @Schema(description = "대표 사진이 있는 날짜만 들어 있다.")
        List<DayCover> dayCovers
) {
    public record Occurrence(
            Long eventId,
            String title,
            Long categoryId,
            @Schema(description = "카테고리 색상. 미분류면 null.")
            String color,
            boolean allDay,
            @Schema(description = "이 회차의 시작 시각")
            LocalDateTime startAt,
            LocalDateTime endAt,
            boolean repeating,
            int photoCount
    ) {
    }

    public record DayCover(
            LocalDate date,
            Long photoId,
            Long eventId,
            String thumbnailUrl
    ) {
    }
}
