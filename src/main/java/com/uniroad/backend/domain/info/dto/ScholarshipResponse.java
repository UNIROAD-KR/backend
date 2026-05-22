package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.Scholarship;

import java.time.LocalDate;
import java.util.List;

public record ScholarshipResponse(
        Long id,
        String name,
        String provider,
        String amount,
        String target,
        String eligibility,
        String description,
        String tips,
        List<String> applicationPeriods,
        List<String> requiredDocuments,
        List<String> essayTips,
        LocalDate deadline,
        String officialUrl
) {
    public static ScholarshipResponse from(Scholarship scholarship) {
        return new ScholarshipResponse(
                scholarship.getId(),
                scholarship.getName(),
                scholarship.getProvider(),
                scholarship.getAmount(),
                scholarship.getTarget(),
                scholarship.getEligibility(),
                scholarship.getDescription(),
                scholarship.getTips(),
                scholarship.getApplicationPeriods(),
                scholarship.getRequiredDocuments(),
                scholarship.getEssayTips(),
                scholarship.getDeadline(),
                scholarship.getOfficialUrl()
        );
    }
}
