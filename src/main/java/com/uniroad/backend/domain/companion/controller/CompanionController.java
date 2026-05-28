package com.uniroad.backend.domain.companion.controller;

import com.uniroad.backend.domain.companion.dto.CompanionPostRequest;
import com.uniroad.backend.domain.companion.dto.CompanionPostResponse;
import com.uniroad.backend.domain.companion.service.CompanionService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Companion", description = "동행 구하기 관련 API")
@RestController
@RequestMapping("/api/companions")
@RequiredArgsConstructor
public class CompanionController {

    private final CompanionService companionService;

    @Operation(summary = "동행 구하기 게시글 작성", description = "새로운 동행 구하기 글을 작성합니다. (인증된 사용자만 가능)")
    @PostMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CompanionPostRequest request
    ) {
        Long id = companionService.createPost(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("게시글이 작성되었습니다.", id));
    }

    @Operation(summary = "동행 구하기 목록 조회", description = "전체 동행 구하기 글 목록을 조회합니다. (인증된 사용자만 가능)")
    @GetMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CompanionPostResponse>>> getPosts() {
        List<CompanionPostResponse> posts = companionService.getPosts();
        return ResponseEntity.ok(ApiResponse.success("동행 구하기 목록 조회 성공", posts));
    }

    @Operation(summary = "내 동행 구하기 글 조회", description = "내가 작성한 동행 구하기 게시글 목록을 조회합니다.")
    @GetMapping("/my")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CompanionPostResponse>>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<CompanionPostResponse> posts = companionService.getMyPosts(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success("내 게시글 목록 조회 성공", posts));
    }

    @Operation(summary = "동행 구하기 상세 조회", description = "게시글 상세 내용을 조회합니다. (인증된 사용자만 가능)")
    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanionPostResponse>> getPostDetail(
            @PathVariable Long postId
    ) {
        CompanionPostResponse post = companionService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회 성공", post));
    }

    @Operation(summary = "동행 구하기 게시글 수정", description = "자신이 작성한 게시글을 수정합니다.")
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody CompanionPostRequest request
    ) {
        companionService.updatePost(userDetails.getMemberId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", null));
    }

    @Operation(summary = "동행 구하기 게시글 삭제", description = "자신이 작성한 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        companionService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }
}
