package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "캘린더 사진 업로드 URL 발급 응답. 요청한 순서와 같다.")
public record CalendarPhotoUploadResponse(List<Item> photos) {

    public record Item(
            @Schema(description = "일정을 저장할 때 photos[].key로 보낼 값")
            String key,
            @Schema(description = "원본을 PUT할 URL. 요청과 같은 Content-Type 헤더로 올려야 한다.")
            String uploadUrl,
            @Schema(description = "일정을 저장할 때 photos[].thumbnailKey로 보낼 값. 썸네일을 올리지 않았으면 보내지 않는다.")
            String thumbnailKey,
            @Schema(description = "썸네일을 PUT할 URL")
            String thumbnailUploadUrl
    ) {
    }
}
