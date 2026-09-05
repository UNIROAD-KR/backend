package com.uniroad.backend.domain.blog.controller;

import com.uniroad.backend.domain.blog.dto.BlogPostDetailResponse;
import com.uniroad.backend.domain.blog.dto.BlogPostLikeResponse;
import com.uniroad.backend.domain.blog.dto.BlogPostSummaryResponse;
import com.uniroad.backend.domain.blog.service.BlogPostService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 누구나 읽는 블로그 API.
 *
 * 조회는 비로그인도 되지만, 로그인 상태면 JWT 필터가 SecurityContext를 채워두므로
 * "내가 좋아요를 눌렀는지"까지 같은 응답에 담아 보낼 수 있다.
 */
@Tag(name = "Blog", description = "블로그 공개 API")
@RestController
@RequestMapping("/api/blog/posts")
@RequiredArgsConstructor
public class BlogController {

    private final BlogPostService blogPostService;

    @Operation(summary = "블로그 목록 조회", description = "공개된 글만 최신순으로 내려줍니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<BlogPostSummaryResponse>>> getPosts(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "9") int size,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "블로그 목록 조회 성공",
                blogPostService.getPublishedPosts(cursorId, size, memberIdOrNull(user))
        ));
    }

    @Operation(summary = "블로그 상세 조회", description = "공개된 글만 조회할 수 있습니다.")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<BlogPostDetailResponse>> getPost(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "블로그 상세 조회 성공",
                blogPostService.getPublishedPost(slug, memberIdOrNull(user))
        ));
    }

    @Operation(summary = "블로그 좋아요 토글", description = "로그인한 회원만 누를 수 있습니다.")
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<BlogPostLikeResponse>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "좋아요 처리 성공",
                blogPostService.toggleLike(postId, user.getMemberId())
        ));
    }

    private Long memberIdOrNull(CustomUserDetails user) {
        return user == null ? null : user.getMemberId();
    }
}
