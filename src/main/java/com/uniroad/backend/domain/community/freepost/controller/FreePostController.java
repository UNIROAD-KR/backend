package com.uniroad.backend.domain.community.freepost.controller;

import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostDetailResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostLikeResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostSummaryResponse;
import com.uniroad.backend.domain.community.freepost.service.FreePostService;
import com.uniroad.backend.global.common.ApiResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "FreePost", description = "자유게시판 관련 API")
@RestController
@RequestMapping("/api/community/free-posts")
@RequiredArgsConstructor
public class FreePostController {

    private final FreePostService freePostService;

    @Operation(summary = "자유게시판 목록 조회")
    @GetMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FreePostSummaryResponse>>> getPosts() {
        return ResponseEntity.ok(ApiResponse.success("자유게시판 목록 조회 성공", freePostService.getPosts()));
    }

    @Operation(summary = "자유게시판 상세 조회")
    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FreePostDetailResponse>> getPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        FreePostDetailResponse response = freePostService.getPost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 상세 조회 성공", response));
    }

    @Operation(summary = "자유게시판 게시글 작성")
    @PostMapping
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FreePostRequest request
    ) {
        Long id = freePostService.createPost(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 게시글 작성 성공", id));
    }

    @Operation(summary = "자유게시판 게시글 수정")
    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody FreePostRequest request
    ) {
        freePostService.updatePost(userDetails.getMemberId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 게시글 수정 성공", null));
    }

    @Operation(summary = "자유게시판 게시글 삭제")
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        freePostService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 게시글 삭제 성공", null));
    }

    @Operation(summary = "자유게시판 좋아요 토글")
    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FreePostLikeResponse>> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        FreePostLikeResponse response = freePostService.toggleLike(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 좋아요 토글 성공", response));
    }

    @Operation(summary = "자유게시판 댓글 작성")
    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FreePostCommentResponse>> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody FreePostCommentRequest request
    ) {
        FreePostCommentResponse response = freePostService.createComment(userDetails.getMemberId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 댓글 작성 성공", response));
    }

    @Operation(summary = "자유게시판 댓글 삭제")
    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('VERIFIED') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        freePostService.deleteComment(userDetails.getMemberId(), postId, commentId);
        return ResponseEntity.ok(ApiResponse.success("자유게시판 댓글 삭제 성공", null));
    }
}
