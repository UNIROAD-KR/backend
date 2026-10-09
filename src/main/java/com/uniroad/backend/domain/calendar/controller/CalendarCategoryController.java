package com.uniroad.backend.domain.calendar.controller;

import com.uniroad.backend.domain.calendar.dto.CalendarCategoryRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarCategoryResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarCategoryUpdateRequest;
import com.uniroad.backend.domain.calendar.service.CalendarCategoryService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Calendar", description = "사진 캘린더 관련 API")
@RestController
@RequestMapping("/api/calendar/categories")
@RequiredArgsConstructor
public class CalendarCategoryController {

    private final CalendarCategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "카테고리가 하나도 없으면 기본 카테고리를 만들어 돌려줍니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarCategoryResponse>>> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<CalendarCategoryResponse> response = categoryService.getCategories(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("카테고리 조회 성공", response));
    }

    @Operation(summary = "카테고리 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<CalendarCategoryResponse>> createCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CalendarCategoryRequest request
    ) {
        CalendarCategoryResponse response = categoryService.createCategory(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("카테고리가 추가되었습니다.", response));
    }

    @Operation(summary = "카테고리 수정", description = "보내지 않은 항목은 그대로 둡니다.")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CalendarCategoryResponse>> updateCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId,
            @Valid @RequestBody CalendarCategoryUpdateRequest request
    ) {
        CalendarCategoryResponse response =
                categoryService.updateCategory(userDetails.getMemberId(), categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("카테고리가 수정되었습니다.", response));
    }

    @Operation(summary = "카테고리 삭제", description = "이 카테고리를 쓰던 일정은 미분류로 남습니다.")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        categoryService.deleteCategory(userDetails.getMemberId(), categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리가 삭제되었습니다.", null));
    }
}
