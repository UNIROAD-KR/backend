package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.UniversityExchangeInfoResponse;
import com.uniroad.backend.domain.info.service.UniversityExchangeInfoService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UniversityExchangeInfo", description = "소속 학교 교환학생 정보 API")
@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityExchangeInfoController {

    private final UniversityExchangeInfoService universityExchangeInfoService;

    @Operation(summary = "학교 교환학생 정보 조회")
    @GetMapping("/{id}/exchange-info")
    public ResponseEntity<ApiResponse<UniversityExchangeInfoResponse>> getExchangeInfo(@PathVariable Long id) {
        UniversityExchangeInfoResponse response = universityExchangeInfoService.getExchangeInfo(id);
        return ResponseEntity.ok(ApiResponse.success("교환학생 정보 조회 성공", response));
    }
}
