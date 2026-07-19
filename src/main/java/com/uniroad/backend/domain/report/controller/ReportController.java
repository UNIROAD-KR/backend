package com.uniroad.backend.domain.report.controller;

import com.uniroad.backend.domain.report.dto.ReportRequest;
import com.uniroad.backend.domain.report.service.ReportService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 생성", description = "자유게시판, 중고거래, 동행, 회원을 신고합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReportRequest request
    ) {
        Long id = reportService.createReport(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("신고가 접수되었습니다.", id));
    }
}
