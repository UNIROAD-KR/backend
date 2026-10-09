package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "캘린더 카테고리 수정 요청. 보내지 않은 항목은 그대로 둔다.")
public record CalendarCategoryUpdateRequest(
        @Schema(description = "카테고리 이름", example = "여행")
        @Size(min = 1, max = 20, message = "카테고리 이름은 1자 이상 20자 이하여야 합니다.")
        String name,

        @Schema(description = "색상 (#RRGGBB)", example = "#34C759")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다.")
        String color
) {
}
