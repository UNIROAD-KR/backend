package com.uniroad.backend.domain.companion.controller;

import com.uniroad.backend.domain.companion.dto.CompanionPostRequest;
import com.uniroad.backend.domain.companion.dto.CompanionPostResponse;
import com.uniroad.backend.domain.companion.service.CompanionService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Companion", description = "동행 구하기 API")
@RestController
@RequestMapping("/api/companions")
@RequiredArgsConstructor
public class CompanionController {

    private final CompanionService companionService;

    @Operation(summary = "동행 구하기 게시글 작성", description = "인증 회원 또는 관리자만 작성할 수 있습니다.")
    @PostMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CompanionPostRequest request
    ) {
        Long id = companionService.createPost(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 게시글 작성 성공", id));
    }

    @Operation(summary = "동행 구하기 목록 조회")
    @GetMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CursorPageResponse<CompanionPostResponse>>> getPosts(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorPageResponse<CompanionPostResponse> posts = companionService.getPosts(cursorId, size);
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 목록 조회 성공", posts));
    }

    @Operation(summary = "내 동행 구하기 글 조회")
    @GetMapping("/my")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CursorPageResponse<CompanionPostResponse>>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorPageResponse<CompanionPostResponse> posts =
                companionService.getMyPosts(userDetails.getMemberId(), cursorId, size);
        return ResponseEntity.ok(ApiResponse.success("내 동행 구하기 글 조회 성공", posts));
    }

    @Operation(summary = "동행 구하기 상세 조회")
    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanionPostResponse>> getPostDetail(
            @PathVariable Long postId
    ) {
        CompanionPostResponse post = companionService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 상세 조회 성공", post));
    }

    @Operation(summary = "동행 구하기 게시글 수정")
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody CompanionPostRequest request
    ) {
        companionService.updatePost(userDetails.getMemberId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 게시글 수정 성공", null));
    }

    @Operation(summary = "동행 구하기 모집 완료", description = "작성자만 모집 완료로 변경할 수 있습니다.")
    @PatchMapping("/{postId}/complete")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> completePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        companionService.completePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 모집 완료 처리 성공", null));
    }

    @Operation(summary = "동행 구하기 게시글 삭제")
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        companionService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 게시글 삭제 성공", null));
    }
}
