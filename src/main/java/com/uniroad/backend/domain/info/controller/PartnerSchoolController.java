package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.PartnerSchoolBookmarkResponse;
import com.uniroad.backend.domain.info.dto.PartnerSchoolDetailResponse;
import com.uniroad.backend.domain.info.dto.PartnerSchoolSummaryResponse;
import com.uniroad.backend.domain.info.service.PartnerSchoolService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PartnerSchool", description = "파견교 정보 API")
@RestController
@RequestMapping("/api/partner-schools")
@RequiredArgsConstructor
public class PartnerSchoolController {

    private final PartnerSchoolService partnerSchoolService;

    @Operation(summary = "파견교 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerSchoolSummaryResponse>>> getPartnerSchools(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PartnerSchoolSummaryResponse> response = partnerSchoolService.getPartnerSchools(keyword, country, pageable);
        return ResponseEntity.ok(ApiResponse.success("파견교 목록 조회 성공", response));
    }

    @Operation(summary = "파견교 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerSchoolDetailResponse>> getPartnerSchool(@PathVariable Long id) {
        PartnerSchoolDetailResponse response = partnerSchoolService.getPartnerSchool(id);
        return ResponseEntity.ok(ApiResponse.success("파견교 상세 조회 성공", response));
    }

    @Operation(summary = "파견교 북마크")
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<PartnerSchoolBookmarkResponse>> bookmark(@PathVariable Long id) {
        PartnerSchoolBookmarkResponse response = partnerSchoolService.bookmark(id);
        return ResponseEntity.ok(ApiResponse.success("파견교 북마크 성공", response));
    }

    @Operation(summary = "파견교 북마크 취소")
    @DeleteMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<PartnerSchoolBookmarkResponse>> unbookmark(@PathVariable Long id) {
        PartnerSchoolBookmarkResponse response = partnerSchoolService.unbookmark(id);
        return ResponseEntity.ok(ApiResponse.success("파견교 북마크 취소 성공", response));
    }
}
