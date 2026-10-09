package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "캘린더 사진 업로드 URL 발급 요청")
public record CalendarPhotoUploadRequest(
        @Schema(description = "올릴 사진 목록 (최대 10장)")
        @NotEmpty(message = "올릴 사진이 없습니다.")
        @Size(max = 10, message = "사진은 한 번에 최대 10장까지 올릴 수 있습니다.")
        @Valid
        List<Item> photos
) {
    public record Item(
            @Schema(description = "원본 Content-Type (image/jpeg, image/png, image/webp)", example = "image/jpeg")
            @NotBlank(message = "contentType은 필수입니다.")
            String contentType,

            @Schema(description = "썸네일 Content-Type. 비우면 원본과 같다고 본다.", example = "image/jpeg")
            String thumbnailContentType
    ) {
    }
}
