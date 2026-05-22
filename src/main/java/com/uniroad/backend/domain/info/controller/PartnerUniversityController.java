package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.PartnerUniversityDetailResponse;
import com.uniroad.backend.domain.info.dto.PartnerUniversitySummaryResponse;
import com.uniroad.backend.domain.info.service.PartnerUniversityService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PartnerUniversity", description = "파견교 정보 API")
@RestController
@RequestMapping("/api/partner-universities")
@RequiredArgsConstructor
public class PartnerUniversityController {

    private final PartnerUniversityService partnerUniversityService;

    @Operation(summary = "파견교 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartnerUniversitySummaryResponse>>> getPartnerUniversities(
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean dormitoryAvailable,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<PartnerUniversitySummaryResponse> response = partnerUniversityService.getPartnerUniversities(
                countryCode,
                keyword,
                major,
                language,
                dormitoryAvailable,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success("파견교 목록 조회 성공", response));
    }

    @Operation(summary = "파견교 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerUniversityDetailResponse>> getPartnerUniversity(@PathVariable Long id) {
        PartnerUniversityDetailResponse response = partnerUniversityService.getPartnerUniversity(id);
        return ResponseEntity.ok(ApiResponse.success("파견교 상세 조회 성공", response));
    }
}
