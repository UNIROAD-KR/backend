package com.uniroad.backend.domain.auth.dto;

import com.uniroad.backend.domain.member.entity.CurrentSituation;
import com.uniroad.backend.domain.member.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "온보딩 요청 데이터")
public record OnboardingRequest(
        @Schema(description = "나이", example = "23")
        Integer age,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Schema(description = "닉네임", example = "유니")
        String nickname,

        @NotNull(message = "성별은 필수입니다.")
        @Schema(description = "성별", example = "FEMALE", allowableValues = {"MALE", "FEMALE"})
        Gender gender,

        @NotNull(message = "현재 상황은 필수입니다.")
        @Schema(description = "현재 상황", example = "PREPARING_APPLICATION",
                allowableValues = {"PREPARING_APPLICATION", "PREPARING_DEPARTURE", "DISPATCHED"})
        CurrentSituation currentSituation,

        @NotBlank(message = "현재 국내 대학은 필수입니다.")
        @Schema(description = "현재 국내 대학", example = "한국대학교")
        String domesticUniversity,

        @Schema(description = "파견 대학", example = "도쿄대학교")
        String dispatchedUniversity,

        @Schema(description = "파견 국가", example = "일본")
        String dispatchedCountry,

        @Schema(description = "파견 지역", example = "도쿄")
        String dispatchedRegion,

        @Schema(description = "파견 연도", example = "2027")
        Integer dispatchYear,

        @Schema(description = "파견 학기", example = "1학기")
        String dispatchSemester,

        @Schema(description = "지원 마감일 (지원중 상태)", example = "2026-09-30")
        LocalDate applicationDeadline,

        @Schema(description = "출국일 (출국 준비중 상태)", example = "2027-02-15")
        LocalDate departureDate,

        @Schema(description = "파견 시작일 (파견중 상태)", example = "2027-03-01")
        LocalDate dispatchStartDate,

        @Schema(description = "귀국일 (파견중 상태)", example = "2027-12-20")
        LocalDate returnDate
) {
}
