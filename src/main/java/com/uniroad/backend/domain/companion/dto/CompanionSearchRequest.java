package com.uniroad.backend.domain.companion.dto;

import com.uniroad.backend.domain.companion.entity.GenderCondition;
import com.uniroad.backend.domain.companion.entity.RecruitmentStatus;
import com.uniroad.backend.global.common.SortOrder;

import java.time.LocalDate;

public record CompanionSearchRequest(
        RecruitmentStatus status,
        String country,
        String region,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo,
        GenderCondition genderCondition,
        Integer minCapacity,
        Integer maxCapacity,
        SortOrder sort
) {
}
