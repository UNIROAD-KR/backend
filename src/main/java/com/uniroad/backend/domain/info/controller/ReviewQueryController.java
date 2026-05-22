package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.ReviewSummaryResponse;
import com.uniroad.backend.domain.info.service.ReviewQueryService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review", description = "파견 후기 조회 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewQueryController {

    private final ReviewQueryService reviewQueryService;

    @Operation(summary = "후기 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewSummaryResponse>>> getReviews(
            @RequestParam(required = false) Long partnerUniversityId,
            @RequestParam(required = false) String countryCode,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ReviewSummaryResponse> response = reviewQueryService.getReviews(
                partnerUniversityId,
                countryCode,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success("후기 목록 조회 성공", response));
    }
}
