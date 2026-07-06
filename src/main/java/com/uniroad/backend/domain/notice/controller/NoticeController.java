package com.uniroad.backend.domain.notice.controller;

import com.uniroad.backend.domain.notice.dto.NoticeRequest;
import com.uniroad.backend.domain.notice.dto.NoticeResponse;
import com.uniroad.backend.domain.notice.service.NoticeService;
import com.uniroad.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notice API", description = "공지사항 API")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회", description = "모든 사용자가 공지사항 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getNotices() {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotices()));
    }

    @Operation(summary = "공지사항 상세 조회", description = "모든 사용자가 공지사항 상세를 조회합니다.")
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNotice(noticeId)));
    }

    @Operation(summary = "공지사항 작성", description = "ADMIN만 공지사항을 작성할 수 있습니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(@Valid @RequestBody NoticeRequest request) {
        NoticeResponse response = noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "공지사항이 등록되었습니다.", response));
    }

    @Operation(summary = "공지사항 수정", description = "ADMIN만 공지사항을 수정할 수 있습니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("공지사항이 수정되었습니다.", noticeService.updateNotice(noticeId, request)));
    }
}
