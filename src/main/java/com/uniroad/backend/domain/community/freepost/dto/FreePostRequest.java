package com.uniroad.backend.domain.community.freepost.dto;

import com.uniroad.backend.domain.community.freepost.entity.FreePostCategory;
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

        @Schema(description = "카테고리 (없으면 작성 시 CHAT, 수정 시 기존 값 유지)", example = "QUESTION", allowableValues = {"QUESTION", "CHAT", "WORRY"})
        FreePostCategory category,

        @Schema(description = "이미지 URL 목록")
        List<String> imageUrls
) {
}
