package com.uniroad.backend.domain.community.freepost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "자유게시판 게시글 작성/수정 요청")
public record FreePostRequest(
        @Schema(description = "제목", example = "제목")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "본문", example = "본문")
        @NotBlank(message = "본문은 필수입니다.")
        String content,

        @Schema(description = "국가", example = "독일")
        @NotBlank(message = "국가는 필수입니다.")
        String country,

        @Schema(description = "상태", example = "파견 중")
        @NotBlank(message = "상태는 필수입니다.")
        String status,

        @Schema(description = "이미지 URL 목록")
        List<String> imageUrls
) {
}
