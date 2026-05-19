package com.uniroad.backend.domain.accountbook.service;

import com.uniroad.backend.domain.accountbook.dto.*;
import com.uniroad.backend.domain.accountbook.entity.AccountBook;
import com.uniroad.backend.domain.accountbook.entity.TransactionType;
import com.uniroad.backend.domain.accountbook.repository.AccountBookRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;
    private final MemberRepository memberRepository;

    /**
     * 가계부 내역 추가 및 잔액 업데이트
     */
    @Transactional
    public Long addTransaction(Long memberId, AccountBookRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 잔액 업데이트
        if (request.type() == TransactionType.INCOME) {
            member.chargeBalance(request.amount());
        } else {
            member.spendBalance(request.amount());
        }

        AccountBook accountBook = AccountBook.builder()
                .member(member)
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .title(request.title())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .build();

        return accountBookRepository.save(accountBook).getId();
    }

    /**
     * 월간 요약 조회
     */
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(Long memberId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AccountBook> transactions = accountBookRepository.findAllByMemberAndMonth(memberId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<LocalDate, MonthlySummaryResponse.DailySummary> dailyMap = new TreeMap<>();

        for (AccountBook tx : transactions) {
            if (tx.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(tx.getAmount());
            } else {
                totalExpense = totalExpense.add(tx.getAmount());
            }

            dailyMap.compute(tx.getTransactionDate(), (date, summary) -> {
                BigDecimal income = (tx.getType() == TransactionType.INCOME) ? tx.getAmount() : BigDecimal.ZERO;
                BigDecimal expense = (tx.getType() == TransactionType.EXPENSE) ? tx.getAmount() : BigDecimal.ZERO;
                
                if (summary == null) {
                    return new MonthlySummaryResponse.DailySummary(income, expense);
                } else {
                    return new MonthlySummaryResponse.DailySummary(
                        summary.income().add(income),
                        summary.expense().add(expense)
                    );
                }
            });
        }

        return MonthlySummaryResponse.of(totalIncome, totalExpense, dailyMap);
    }

    /**
     * 특정 날짜 상세 내역 조회
     */
    @Transactional(readOnly = true)
    public List<AccountBookResponse> getDailyDetails(Long memberId, LocalDate date) {
        return accountBookRepository.findByMemberIdAndTransactionDate(memberId, date)
                .stream()
                .map(AccountBookResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 가계부 잔액 조회
     */
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return BalanceResponse.of(member.getBalance());
    }
}
