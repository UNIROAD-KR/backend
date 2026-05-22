package com.uniroad.backend.domain.info.controller;

import com.uniroad.backend.domain.info.dto.DocumentCheckRequest;
import com.uniroad.backend.domain.info.dto.DocumentCheckResponse;
import com.uniroad.backend.domain.info.dto.MyUniversityExchangeInfoResponse;
import com.uniroad.backend.domain.info.service.MyUniversityExchangeInfoService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyUniversityExchangeInfo", description = "내 학교 교환학생 정보 API")
@RestController
@RequestMapping("/api/my-university/exchange-info")
@RequiredArgsConstructor
public class MyUniversityExchangeInfoController {

    private final MyUniversityExchangeInfoService myUniversityExchangeInfoService;

    @Operation(summary = "내 학교 교환학생 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<MyUniversityExchangeInfoResponse>> getMyExchangeInfo() {
        MyUniversityExchangeInfoResponse response = myUniversityExchangeInfoService.getMyExchangeInfo();
        return ResponseEntity.ok(ApiResponse.success("내 학교 교환학생 정보 조회 성공", response));
    }

    @Operation(summary = "내 학교 체크리스트 저장")
    @PatchMapping("/documents/{documentId}")
    public ResponseEntity<ApiResponse<DocumentCheckResponse>> updateDocumentCheck(
            @PathVariable Long documentId,
            @RequestBody DocumentCheckRequest request
    ) {
        DocumentCheckResponse response = myUniversityExchangeInfoService.updateDocumentCheck(documentId, request);
        return ResponseEntity.ok(ApiResponse.success("체크리스트 저장 성공", response));
    }
}
