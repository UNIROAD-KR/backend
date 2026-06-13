package com.uniroad.backend.domain.member.dto;

import com.uniroad.backend.domain.member.entity.CurrentSituation;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberProfileUpdateRequest(
        CurrentSituation currentSituation,

        @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,

        @Size(max = 100, message = "파견 대학교는 100자 이하여야 합니다.")
        String dispatchedUniversity,

        @Size(max = 100, message = "파견 국가는 100자 이하여야 합니다.")
        String dispatchedCountry,

        @Size(max = 100, message = "현재 대학교는 100자 이하여야 합니다.")
        String domesticUniversity,

        Integer dispatchYear,

        @Size(max = 30, message = "파견 학기는 30자 이하여야 합니다.")
        String dispatchSemester,

        LocalDate applicationDeadline,

        LocalDate departureDate,

        LocalDate dispatchStartDate,

        LocalDate returnDate
) {
}
