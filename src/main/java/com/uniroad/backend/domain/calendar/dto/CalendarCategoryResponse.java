package com.uniroad.backend.domain.calendar.dto;

import com.uniroad.backend.domain.calendar.entity.CalendarCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "캘린더 카테고리 응답")
public record CalendarCategoryResponse(
        Long id,
        String name,
        String color
) {
    public static CalendarCategoryResponse from(CalendarCategory category) {
        return new CalendarCategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}
