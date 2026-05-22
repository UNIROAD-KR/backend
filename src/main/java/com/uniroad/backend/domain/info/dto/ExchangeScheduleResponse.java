package com.uniroad.backend.domain.info.dto;

import com.uniroad.backend.domain.info.entity.ExchangeSchedule;

import java.time.LocalDate;

public record ExchangeScheduleResponse(
        String title,
        LocalDate startDate,
        LocalDate endDate
) {
    public static ExchangeScheduleResponse from(ExchangeSchedule schedule) {
        return new ExchangeScheduleResponse(
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate()
        );
    }
}
