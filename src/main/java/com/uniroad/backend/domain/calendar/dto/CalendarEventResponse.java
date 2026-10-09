package com.uniroad.backend.domain.calendar.dto;

import com.uniroad.backend.domain.calendar.entity.CalendarReminder;
import com.uniroad.backend.domain.calendar.entity.CalendarRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일정 상세 응답")
public record CalendarEventResponse(
        Long id,
        Long categoryId,
        String categoryName,
        @Schema(description = "카테고리 색상. 미분류면 null.")
        String color,
        String title,
        boolean allDay,
        @Schema(description = "일정을 등록한 원래 시작 시각. 수정 화면은 이 값을 쓴다.")
        LocalDateTime startAt,
        LocalDateTime endAt,
        @Schema(description = "이번 회차의 시작 시각. 반복하지 않는 일정은 startAt과 같다.")
        LocalDateTime occurrenceStartAt,
        LocalDateTime occurrenceEndAt,
        CalendarReminder reminder,
        CalendarRepeatType repeatType,
        LocalDate repeatUntil,
        String memo,
        List<CalendarPhotoResponse> photos
) {
}
