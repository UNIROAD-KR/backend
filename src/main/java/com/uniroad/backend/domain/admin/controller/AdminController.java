package com.uniroad.backend.domain.admin.controller;

import com.uniroad.backend.domain.admin.service.AdminService;
import com.uniroad.backend.domain.admin.dto.AdminDashboardResponse;
import com.uniroad.backend.domain.member.dto.MemberResponseDto;
import com.uniroad.backend.domain.member.dto.MemberRoleUpdateRequest;
import com.uniroad.backend.domain.report.dto.AdminReportUpdateRequest;
import com.uniroad.backend.domain.report.dto.ReportResponse;
import com.uniroad.backend.domain.report.service.ReportService;
import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin API", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @Operation(summary = "공지사항 삭제", description = "관리자가 공지사항을 삭제합니다.")
    @DeleteMapping("/notices/{noticeId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long noticeId) {
        adminService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.success("공지사항이 삭제되었습니다.", null));
    }

    @Operation(summary = "회원 목록 조회", description = "모든 회원을 조회합니다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getMembers() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getMembers()));
    }

    @Operation(summary = "회원 삭제", description = "회원과 관련 데이터를 모두 삭제합니다.")
    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long memberId) {
        adminService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("회원이 삭제되었습니다.", null));
    }

    @Operation(summary = "회원 등급 변경", description = "회원의 역할(Role)을 변경합니다.")
    @PatchMapping("/members/{memberId}/role")
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateMemberRole(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request
    ) {
        MemberResponseDto response = adminService.updateMemberRole(memberId, request);
        return ResponseEntity.ok(ApiResponse.success("회원 등급이 변경되었습니다.", response));
    }

    @Operation(summary = "승인 완료된 인증 목록 조회", description = "승인 완료된 인증 요청 목록을 조회합니다.")
    @GetMapping("/verifications/approved")
    public ResponseEntity<ApiResponse<List<AdminVerificationResponse>>> getApprovedVerifications() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getApprovedVerifications()));
    }

    @Operation(summary = "거절된 인증 목록 조회", description = "거절된 인증 요청 목록을 조회합니다.")
    @GetMapping("/verifications/rejected")
    public ResponseEntity<ApiResponse<List<AdminVerificationResponse>>> getRejectedVerifications() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getRejectedVerifications()));
    }

    @Operation(summary = "신고 목록 조회", description = "관리자가 모든 신고 목록을 조회합니다.")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAllReports()));
    }

    @Operation(summary = "신고 상태 변경", description = "관리자가 신고 상태를 변경합니다.")
    @PatchMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminReportUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("신고 상태가 변경되었습니다.", reportService.updateReportStatus(id, request)));
    }

    @Operation(summary = "관리자 대시보드 조회", description = "회원 수, 게시글 수, 신고 수를 조회합니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }
}
