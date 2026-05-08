package com.uniroad.backend.domain.accountbook.dto;

import com.uniroad.backend.domain.accountbook.entity.AccountBook;
import com.uniroad.backend.domain.accountbook.entity.AccountCategory;
import com.uniroad.backend.domain.accountbook.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "가계부 상세 내역 응답")
public record AccountBookResponse(
    Long id,
    java.math.BigDecimal amount,
    TransactionType type,
    AccountCategory category,
    String categoryName,
    String title,
    String description,
    LocalDate transactionDate
) {
    public static AccountBookResponse from(AccountBook accountBook) {
        return new AccountBookResponse(
            accountBook.getId(),
            accountBook.getAmount(),
            accountBook.getType(),
            accountBook.getCategory(),
            accountBook.getCategory().getDescription(),
            accountBook.getTitle(),
            accountBook.getDescription(),
            accountBook.getTransactionDate()
        );
    }
}
