package com.uniroad.backend.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 사진. URL은 1시간 동안만 유효하고 조회할 때마다 바뀌므로, 앱은 id를 캐시 키로 써야 한다.")
public record CalendarPhotoResponse(
        Long id,
        String imageUrl,
        String thumbnailUrl,
        Integer width,
        Integer height
) {
}
