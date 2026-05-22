package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.ExchangeReviewResponse;
import com.uniroad.backend.domain.info.dto.ReviewCommentRequest;
import com.uniroad.backend.domain.info.dto.ReviewCommentResponse;
import com.uniroad.backend.domain.info.dto.ReviewLikeResponse;
import com.uniroad.backend.domain.info.service.ExchangeReviewService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "ExchangeReview", description = "정보 탐색 후기 API")
@RestController
@RequestMapping("/api/exchange-reviews")
@RequiredArgsConstructor
public class ExchangeReviewController {

    private final ExchangeReviewService exchangeReviewService;

    @Operation(summary = "정보 탐색 후기 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExchangeReviewResponse>>> getReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ExchangeReviewResponse> response = exchangeReviewService.getReviews(keyword, country, type, pageable);
        return ResponseEntity.ok(ApiResponse.success("후기 목록 조회 성공", response));
    }

    @Operation(summary = "정보 탐색 후기 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExchangeReviewResponse>> getReview(@PathVariable Long id) {
        ExchangeReviewResponse response = exchangeReviewService.getReview(id);
        return ResponseEntity.ok(ApiResponse.success("후기 상세 조회 성공", response));
    }

    @Operation(summary = "후기 댓글 목록 조회")
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<ReviewCommentResponse>>> getComments(@PathVariable Long id) {
        List<ReviewCommentResponse> response = exchangeReviewService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success("후기 댓글 조회 성공", response));
    }

    @Operation(summary = "후기 댓글 작성")
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<ReviewCommentResponse>> createComment(
            @PathVariable Long id,
            @Valid @RequestBody ReviewCommentRequest request
    ) {
        ReviewCommentResponse response = exchangeReviewService.createComment(id, request);
        return ResponseEntity.ok(ApiResponse.success("후기 댓글 작성 성공", response));
    }

    @Operation(summary = "후기 좋아요")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<ReviewLikeResponse>> like(@PathVariable Long id) {
        ReviewLikeResponse response = exchangeReviewService.like(id);
        return ResponseEntity.ok(ApiResponse.success("후기 좋아요 성공", response));
    }

    @Operation(summary = "후기 좋아요 취소")
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<ReviewLikeResponse>> unlike(@PathVariable Long id) {
        ReviewLikeResponse response = exchangeReviewService.unlike(id);
        return ResponseEntity.ok(ApiResponse.success("후기 좋아요 취소 성공", response));
    }
}
