package com.uniroad.backend.global.infra.s3.controller;

import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.infra.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "S3", description = "S3 파일 업로드 관련 API")
@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "S3 업로드용 Presigned URL 발급", description = "이미지 업로드를 위한 Presigned URL을 발급받습니다.")
    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(@RequestParam String fileName) {
        String presignedUrl = s3Service.getPresignedUrl(fileName);
        return ResponseEntity.ok(ApiResponse.success("Presigned URL 발급 성공", presignedUrl));
    }
}
