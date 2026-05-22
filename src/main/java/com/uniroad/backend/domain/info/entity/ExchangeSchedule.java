package com.uniroad.backend.domain.info.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeSchedule {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
}
