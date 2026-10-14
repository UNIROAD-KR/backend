package com.uniroad.backend.domain.accountbook.controller;

import com.uniroad.backend.domain.accountbook.dto.*;
import com.uniroad.backend.domain.accountbook.service.AccountBookService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "AccountBook", description = "가계부 관련 API")
@RestController
@RequestMapping("/api/account-book")
@RequiredArgsConstructor
public class AccountBookController {

    private final AccountBookService accountBookService;

    @Operation(summary = "가계부 내역 추가", description = "수입(충전) 또는 지출 내역을 추가하고 잔액을 업데이트합니다. 잔액보다 많이 쓰면 잔액이 음수가 됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> addTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AccountBookRequest request
    ) {
        Long id = accountBookService.addTransaction(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("내역이 추가되었습니다.", id));
    }

    @Operation(
            summary = "가계부 내역 수정",
            description = "모든 항목을 통째로 바꾸고, 바뀐 만큼 잔액을 다시 맞춥니다. 잔액은 음수가 될 수 있습니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountBookResponse>> updateTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AccountBookRequest request
    ) {
        AccountBookResponse response = accountBookService.updateTransaction(userDetails.getMemberId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("내역이 수정되었습니다.", response));
    }

    @Operation(
            summary = "가계부 내역 삭제",
            description = "내역을 지우고 그 금액만큼 잔액을 되돌립니다. 잔액은 음수가 될 수 있습니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        accountBookService.deleteTransaction(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success("내역이 삭제되었습니다.", null));
    }

    @Operation(summary = "월간 요약 조회", description = "지정한 달의 날짜별 소비/충전액과 총액을 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MonthlySummaryResponse>> getMonthlySummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month
    ) {
        MonthlySummaryResponse summary = accountBookService.getMonthlySummary(userDetails.getMemberId(), year, month);
        return ResponseEntity.ok(ApiResponse.success("월간 요약 조회 성공", summary));
    }

    @Operation(summary = "일간 상세 조회", description = "지정한 날짜의 전체 소비/충전 내역을 조회합니다.")
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<AccountBookResponse>>> getDailyDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AccountBookResponse> details = accountBookService.getDailyDetails(userDetails.getMemberId(), date);
        return ResponseEntity.ok(ApiResponse.success("일간 상세 조회 성공", details));
    }

    @Operation(summary = "현재 잔액 조회", description = "인증된 사용자의 가계부 현재 잔액을 조회합니다.")
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BalanceResponse balanceResponse = accountBookService.getBalance(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("잔액 조회 성공", balanceResponse));
    }
}
