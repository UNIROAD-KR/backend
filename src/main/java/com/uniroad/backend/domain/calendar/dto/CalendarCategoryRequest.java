package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "캘린더 카테고리 추가 요청")
public record CalendarCategoryRequest(
        @Schema(description = "카테고리 이름", example = "여행")
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 20, message = "카테고리 이름은 20자 이하여야 합니다.")
        String name,

        @Schema(description = "색상 (#RRGGBB)", example = "#34C759")
        @NotBlank(message = "색상은 필수입니다.")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다.")
        String color
) {
}
