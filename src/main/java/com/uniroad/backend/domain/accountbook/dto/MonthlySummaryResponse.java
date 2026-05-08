package com.uniroad.backend.domain.accountbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "월간 가계부 요약 정보")
public record MonthlySummaryResponse(
    @Schema(description = "해당 월 총 충전액")
    BigDecimal totalIncome,

    @Schema(description = "해당 월 총 소비액")
    BigDecimal totalExpense,

    @Schema(description = "날짜별 요약 (날짜 -> {income, expense})")
    Map<LocalDate, DailySummary> dailySummaries
) {
    public record DailySummary(
        BigDecimal income,
        BigDecimal expense
    ) {}

    public static MonthlySummaryResponse of(BigDecimal totalIncome, BigDecimal totalExpense, Map<LocalDate, DailySummary> dailySummaries) {
        return new MonthlySummaryResponse(totalIncome, totalExpense, dailySummaries);
    }
}
