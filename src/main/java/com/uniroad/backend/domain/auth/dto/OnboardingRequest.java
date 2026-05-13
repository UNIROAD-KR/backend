package com.uniroad.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온보딩 요청 데이터")
public record OnboardingRequest(
        @Schema(description = "나이", example = "23")
        Integer age,

        @Schema(description = "파견 대학", example = "도쿄대학")
        String dispatchedUniversity,

        @Schema(description = "파견 국가", example = "일본")
        String dispatchedCountry,

        @Schema(description = "파견 지역", example = "도쿄")
        String dispatchedRegion
) {}
