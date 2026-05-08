package com.uniroad.backend.domain.accountbook.dto;

import com.uniroad.backend.domain.accountbook.entity.AccountCategory;
import com.uniroad.backend.domain.accountbook.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "가계부 내역 추가 요청")
public record AccountBookRequest(
    @Schema(description = "금액", example = "10.50")
    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0원 이상이어야 합니다.")
    java.math.BigDecimal amount,

    @Schema(description = "거래 유형 (INCOME, EXPENSE)", example = "EXPENSE")
    @NotNull(message = "거래 유형은 필수입니다.")
    TransactionType type,

    @Schema(description = "카테고리 (FOOD, TRANSPORT, SHOPPING, TRAVEL, ETC, CHARGE)", example = "FOOD")
    @NotNull(message = "카테고리는 필수입니다.")
    AccountCategory category,

    @Schema(description = "내역 이름", example = "점심 식사")
    @NotBlank(message = "내역 이름은 필수입니다.")
    String title,

    @Schema(description = "설명 (선택)", example = "김치찌개 먹음")
    String description,

    @Schema(description = "거래 날짜", example = "2024-05-08")
    @NotNull(message = "거래 날짜는 필수입니다.")
    LocalDate transactionDate
) {
}
