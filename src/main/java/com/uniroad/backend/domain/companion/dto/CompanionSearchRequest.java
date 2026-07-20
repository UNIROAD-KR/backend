package com.uniroad.backend.domain.companion.dto;

import com.uniroad.backend.domain.companion.entity.RecruitmentStatus;

import java.time.LocalDate;

public record CompanionSearchRequest(
        RecruitmentStatus status,
        String country,
        String region,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo
) {
}
