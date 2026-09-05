package com.uniroad.backend.domain.blog.controller;

import com.uniroad.backend.domain.blog.dto.BlogPostDetailResponse;
import com.uniroad.backend.domain.blog.dto.BlogPostRequest;
import com.uniroad.backend.domain.blog.dto.BlogPostSummaryResponse;
import com.uniroad.backend.domain.blog.service.BlogPostService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 관리자 전용 블로그 API.
 * /api/blog/**와 달리 permitAll 목록에 넣지 않으며, 메서드마다 ADMIN 권한을 요구한다.
 */
@Tag(name = "Admin Blog", description = "블로그 관리 API")
@RestController
@RequestMapping("/api/admin/blog/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {

    private final BlogPostService blogPostService;

    @Operation(summary = "블로그 글 목록 조회", description = "초안을 포함한 전체 목록입니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlogPostSummaryResponse>>> getPosts() {
        return ResponseEntity.ok(ApiResponse.success(blogPostService.getAllPostsForAdmin()));
    }

    @Operation(summary = "블로그 글 단건 조회", description = "에디터 복원용 contentJson을 포함합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(blogPostService.getPostForAdmin(postId)));
    }

    @Operation(summary = "블로그 글 작성")
    @PostMapping
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> createPost(
            @Valid @RequestBody BlogPostRequest request
    ) {
        BlogPostDetailResponse response =
                blogPostService.createPost(request, SecurityUtil.getCurrentMemberId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "블로그 글이 등록되었습니다.", response));
    }

    @Operation(summary = "블로그 글 수정")
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody BlogPostRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "블로그 글이 수정되었습니다.",
                blogPostService.updatePost(postId, request)
        ));
    }

    @Operation(summary = "블로그 글 공개")
    @PostMapping("/{postId}/publish")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> publish(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                "블로그 글을 공개했습니다.",
                blogPostService.setPublished(postId, true)
        ));
    }

    @Operation(summary = "블로그 글 비공개")
    @PostMapping("/{postId}/unpublish")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> unpublish(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                "블로그 글을 초안으로 내렸습니다.",
                blogPostService.setPublished(postId, false)
        ));
    }

    @Operation(summary = "블로그 글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        blogPostService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success("블로그 글을 삭제했습니다.", null));
    }
}
