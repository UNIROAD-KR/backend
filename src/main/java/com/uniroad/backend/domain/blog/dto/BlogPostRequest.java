package com.uniroad.backend.domain.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "블로그 글 생성/수정 요청")
public record BlogPostRequest(
        @Schema(description = "제목", example = "교환학생 준비, 무엇부터 해야 할까")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Schema(description = "공개 URL에 쓰는 식별자. 비우면 서버가 만든다.", example = "exchange-prep-guide")
        @Size(max = 200, message = "slug는 200자 이하여야 합니다.")
        String slug,

        @Schema(description = "목록 카드 설명. 비우면 본문에서 자동으로 채운다.")
        @Size(max = 300, message = "설명은 300자 이하여야 합니다.")
        String summary,

        @Schema(description = "목록 카드 이미지. 비우면 본문 첫 이미지를 쓴다.")
        @Size(max = 500, message = "이미지 주소가 너무 깁니다.")
        String thumbnailUrl,

        @Schema(description = "에디터 원본(ProseMirror JSON)")
        @NotNull(message = "본문은 필수입니다.")
        Map<String, Object> contentJson,

        @Schema(description = "표현용 HTML. 서버가 허용 태그만 남기고 소독한다.")
        @NotBlank(message = "본문은 필수입니다.")
        String contentHtml,

        @Schema(description = "true면 공개, false면 초안으로 저장")
        boolean published
) {
}
