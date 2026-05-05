package com.uniroad.backend.domain.verification.controller;

import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.domain.verification.dto.RejectRequest;
import com.uniroad.backend.domain.verification.dto.VerificationRequest;
import com.uniroad.backend.domain.verification.dto.VerificationResponse;
import com.uniroad.backend.domain.verification.service.VerificationService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Verification", description = "인증(학생/기관) 관련 API")
@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @Operation(summary = "인증 요청 제출", description = "학생 또는 기관 인증 이미지를 제출합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<VerificationResponse>> submitVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody VerificationRequest request
    ) {
        VerificationResponse response = verificationService.submitVerification(userDetails.getMemberId(), request.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success("인증 요청이 제출되었습니다.", response));
    }

    @Operation(summary = "대기 중인 인증 목록 조회 (관리자)", description = "승인이 필요한 모든 인증 요청 목록을 조회합니다.")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminVerificationResponse>>> getPendingVerifications() {
        List<AdminVerificationResponse> response = verificationService.getPendingVerifications();
        return ResponseEntity.ok(ApiResponse.success("대기 중인 인증 목록을 조회했습니다.", response));
    }

    @Operation(summary = "인증 승인 (관리자)", description = "특정 인증 요청을 승인합니다.")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveVerification(@PathVariable Long id) {
        verificationService.approveVerification(id);
        return ResponseEntity.ok(ApiResponse.success("인증 요청이 승인되었습니다.", null));
    }

    @Operation(summary = "인증 거절 (관리자)", description = "특정 인증 요청을 거절합니다.")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectVerification(
            @PathVariable Long id,
            @RequestBody RejectRequest request
    ) {
        verificationService.rejectVerification(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success("인증 요청이 거절되었습니다.", null));
    }
}
