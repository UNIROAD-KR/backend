package com.uniroad.backend.domain.calendar.dto;

import com.uniroad.backend.domain.calendar.entity.CalendarReminder;
import com.uniroad.backend.domain.calendar.entity.CalendarRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일정 추가·수정 요청")
public record CalendarEventRequest(
        @Schema(description = "카테고리 ID. 비우면 미분류.", example = "3")
        Long categoryId,

        @Schema(description = "제목. 사진이 있으면 비울 수 있다.", example = "파리 여행")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "하루 종일. true면 시작·종료의 시각은 버리고 날짜만 쓴다.")
        boolean allDay,

        @Schema(description = "시작 (타임존 없는 현지 시각)", example = "2026-09-21T09:00:00")
        @NotNull(message = "시작 시각은 필수입니다.")
        LocalDateTime startAt,

        @Schema(description = "종료 (타임존 없는 현지 시각)", example = "2026-09-21T10:00:00")
        @NotNull(message = "종료 시각은 필수입니다.")
        LocalDateTime endAt,

        @Schema(description = "알림 (NONE, AT_TIME, MIN_10, HOUR_1, DAY_1). 비우면 NONE.", example = "NONE")
        CalendarReminder reminder,

        @Schema(description = "반복 (NONE, DAILY, WEEKLY, MONTHLY, YEARLY). 비우면 NONE.", example = "NONE")
        CalendarRepeatType repeatType,

        @Schema(description = "반복 종료일. 비우면 끝없이 반복한다.", example = "2026-12-31")
        LocalDate repeatUntil,

        @Schema(description = "메모")
        @Size(max = 5000, message = "메모는 5000자 이하여야 합니다.")
        String memo,

        @Schema(description = "사진 목록 (최대 10장). 배열 순서가 곧 사진 순서다. "
                + "수정할 때는 남길 사진을 모두 보내야 하며, 빠진 사진은 삭제된다.")
        List<Photo> photos
) {
    @Schema(description = "기존 사진은 photoId만, 새 사진은 업로드 URL 발급 때 받은 key를 보낸다.")
    public record Photo(
            @Schema(description = "이미 이 일정에 붙어 있는 사진의 ID")
            Long photoId,
            @Schema(description = "새로 올린 원본의 key")
            String key,
            @Schema(description = "새로 올린 썸네일의 key. 썸네일을 올리지 않았으면 비운다.")
            String thumbnailKey,
            Integer width,
            Integer height
    ) {
    }
}
