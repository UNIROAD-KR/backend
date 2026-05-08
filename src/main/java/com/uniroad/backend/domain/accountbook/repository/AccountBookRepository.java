package com.uniroad.backend.domain.accountbook.repository;

import com.uniroad.backend.domain.accountbook.entity.AccountBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {

    List<AccountBook> findByMemberIdAndTransactionDateBetween(
            Long memberId, LocalDate startDate, LocalDate endDate);

    List<AccountBook> findByMemberIdAndTransactionDate(Long memberId, LocalDate date);

    @Query("SELECT ab FROM AccountBook ab WHERE ab.member.id = :memberId " +
           "AND ab.transactionDate BETWEEN :startDate AND :endDate")
    List<AccountBook> findAllByMemberAndMonth(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
