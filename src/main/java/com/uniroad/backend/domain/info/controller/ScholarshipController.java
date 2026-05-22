package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.ScholarshipResponse;
import com.uniroad.backend.domain.info.service.ScholarshipService;
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

@Tag(name = "Scholarship", description = "장학금 정보 API")
@RestController
@RequestMapping("/api/scholarships")
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    @Operation(summary = "장학금 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ScholarshipResponse>>> getScholarships(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ScholarshipResponse> response = scholarshipService.getScholarships(country, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("장학금 목록 조회 성공", response));
    }

    @Operation(summary = "장학금 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> getScholarship(@PathVariable Long id) {
        ScholarshipResponse response = scholarshipService.getScholarship(id);
        return ResponseEntity.ok(ApiResponse.success("장학금 상세 조회 성공", response));
    }
}
