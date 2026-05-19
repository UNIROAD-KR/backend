package com.uniroad.backend.domain.accountbook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "가계부 잔액 조회 응답 데이터")
public record BalanceResponse(
        @Schema(description = "현재 가계부 잔액", example = "150.50")
        BigDecimal balance
) {
    public static BalanceResponse of(BigDecimal balance) {
        return new BalanceResponse(balance);
    }
}
