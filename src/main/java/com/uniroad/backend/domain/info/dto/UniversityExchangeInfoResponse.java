package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.UniversityExchangeInfo;

import java.util.List;

public record UniversityExchangeInfoResponse(
        String universityName,
        String eligibility,
        String creditPolicy,
        List<String> requiredDocuments,
        String internationalOfficeUrl,
        List<ExchangeScheduleResponse> schedules
) {
    public static UniversityExchangeInfoResponse from(UniversityExchangeInfo exchangeInfo) {
        List<ExchangeScheduleResponse> schedules = exchangeInfo.getSchedules() == null
                ? List.of()
                : exchangeInfo.getSchedules().stream()
                .map(ExchangeScheduleResponse::from)
                .toList();

        return new UniversityExchangeInfoResponse(
                exchangeInfo.getUniversity().getName(),
                exchangeInfo.getEligibility(),
                exchangeInfo.getCreditPolicy(),
                exchangeInfo.getRequiredDocuments(),
                exchangeInfo.getInternationalOfficeUrl(),
                schedules
        );
    }
}
