package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.Scholarship;

import java.time.LocalDate;

public record ScholarshipResponse(
        Long id,
        String name,
        String provider,
        String amount,
        LocalDate deadline,
        String officialUrl
) {
    public static ScholarshipResponse from(Scholarship scholarship) {
        return new ScholarshipResponse(
                scholarship.getId(),
                scholarship.getName(),
                scholarship.getProvider(),
                scholarship.getAmount(),
                scholarship.getDeadline(),
                scholarship.getOfficialUrl()
        );
    }
}
