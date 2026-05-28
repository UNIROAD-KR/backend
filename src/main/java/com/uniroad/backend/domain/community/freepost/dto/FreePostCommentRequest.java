package com.uniroad.backend.domain.community.freepost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "자유게시판 댓글 작성 요청")
public record FreePostCommentRequest(
        @Schema(description = "댓글 내용", example = "댓글 내용")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content
) {
}
