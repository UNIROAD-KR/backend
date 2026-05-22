package com.uniroad.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "온보딩 요청 데이터")
public record OnboardingRequest(
        @Schema(description = "나이", example = "23")
        Integer age,

        @NotBlank(message = "현재 국내 대학은 필수입니다.")
        @Schema(description = "현재 국내 대학", example = "한국대학교")
        String domesticUniversity,

        @Schema(description = "파견 대학", example = "도쿄대학교")
        String dispatchedUniversity,

        @Schema(description = "파견 국가", example = "일본")
        String dispatchedCountry,

        @Schema(description = "파견 지역", example = "도쿄")
        String dispatchedRegion
) {
}
