package com.uniroad.backend.domain.community.freepost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "자유게시판 댓글 작성/수정 요청")
public record FreePostCommentRequest(
        @Schema(description = "댓글 내용", example = "댓글 내용")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,

        @Schema(description = "답글을 달 원댓글 ID. 일반 댓글이면 비운다. 답글에는 답글을 달 수 없다. 수정할 때는 무시한다.",
                example = "1", nullable = true)
        Long parentId
) {
}
