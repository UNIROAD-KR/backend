package com.uniroad.backend.global.infra.s3.controller;

import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.infra.s3.dto.PresignedUrlRequestDto;
import com.uniroad.backend.global.infra.s3.dto.PresignedUrlResponseDto;
import com.uniroad.backend.global.infra.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "S3", description = "S3 업로드 관련 API")
@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "Presigned URL 발급")
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponseDto>> getPresignedUrl(
            @Valid @RequestBody PresignedUrlRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Presigned URL 발급 성공",
                        s3Service.getPresignedUrl(requestDto)
                )
        );
    }
}