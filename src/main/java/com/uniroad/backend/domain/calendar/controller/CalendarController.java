package com.uniroad.backend.domain.calendar.controller;

import com.uniroad.backend.domain.calendar.dto.CalendarDayCoverRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarDayResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarEventRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarEventResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarEventSearchResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarMonthResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadResponse;
import com.uniroad.backend.domain.calendar.service.CalendarService;
import com.uniroad.backend.global.common.ApiResponse;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Calendar", description = "사진 캘린더 관련 API")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(
            summary = "캘린더 메인 조회",
            description = "화면에 보이는 날짜 범위(이전·다음 달 날짜 포함)의 일정과 날짜별 대표 사진을 조회합니다. "
                    + "from과 to는 둘 다 포함하며 최대 100일입니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<CalendarMonthResponse>> getMonth(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        CalendarMonthResponse response = calendarService.getMonth(userDetails.getMemberId(), from, to);
        return ResponseEntity.ok(ApiResponse.success("캘린더 조회 성공", response));
    }

    @Operation(summary = "날짜 상세 조회", description = "선택한 날짜에 걸친 일정과 그 사진 전체를 조회합니다.")
    @GetMapping("/days/{date}")
    public ResponseEntity<ApiResponse<CalendarDayResponse>> getDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        CalendarDayResponse response = calendarService.getDay(userDetails.getMemberId(), date);
        return ResponseEntity.ok(ApiResponse.success("날짜 상세 조회 성공", response));
    }

    @Operation(summary = "대표 사진 변경", description = "그 날짜에 걸친 일정의 사진 중 하나를 대표 사진으로 지정합니다.")
    @PutMapping("/days/{date}/cover")
    public ResponseEntity<ApiResponse<Void>> setDayCover(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody CalendarDayCoverRequest request
    ) {
        calendarService.setDayCover(userDetails.getMemberId(), date, request.photoId());
        return ResponseEntity.ok(ApiResponse.success("대표 사진이 변경되었습니다.", null));
    }

    @Operation(summary = "대표 사진 초기화", description = "직접 고른 대표 사진을 지우고 기본 규칙으로 되돌립니다.")
    @DeleteMapping("/days/{date}/cover")
    public ResponseEntity<ApiResponse<Void>> resetDayCover(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        calendarService.resetDayCover(userDetails.getMemberId(), date);
        return ResponseEntity.ok(ApiResponse.success("대표 사진이 초기화되었습니다.", null));
    }

    @Operation(summary = "일정 추가")
    @PostMapping("/events")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> createEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CalendarEventRequest request
    ) {
        CalendarEventResponse response = calendarService.createEvent(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("일정이 추가되었습니다.", response));
    }

    @Operation(summary = "일정 검색", description = "제목과 메모에서 찾아 최근 일정부터 돌려줍니다.")
    @GetMapping("/events/search")
    public ResponseEntity<ApiResponse<CursorPageResponse<CalendarEventSearchResponse>>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer size
    ) {
        CursorPageResponse<CalendarEventSearchResponse> response =
                calendarService.search(userDetails.getMemberId(), keyword, cursorId, size);
        return ResponseEntity.ok(ApiResponse.success("일정 검색 성공", response));
    }

    @Operation(summary = "일정 상세 조회")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> getEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long eventId
    ) {
        CalendarEventResponse response = calendarService.getEvent(userDetails.getMemberId(), eventId);
        return ResponseEntity.ok(ApiResponse.success("일정 조회 성공", response));
    }

    @Operation(
            summary = "일정 수정",
            description = "모든 항목을 통째로 바꿉니다. photos에서 빠진 사진은 삭제됩니다. 반복 일정은 전체 회차가 함께 바뀝니다."
    )
    @PutMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<CalendarEventResponse>> updateEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long eventId,
            @Valid @RequestBody CalendarEventRequest request
    ) {
        CalendarEventResponse response = calendarService.updateEvent(userDetails.getMemberId(), eventId, request);
        return ResponseEntity.ok(ApiResponse.success("일정이 수정되었습니다.", response));
    }

    @Operation(summary = "일정 삭제", description = "반복 일정은 전체 회차가 함께 삭제됩니다.")
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long eventId
    ) {
        calendarService.deleteEvent(userDetails.getMemberId(), eventId);
        return ResponseEntity.ok(ApiResponse.success("일정이 삭제되었습니다.", null));
    }

    @Operation(
            summary = "사진 업로드 URL 발급",
            description = "사진마다 원본과 썸네일을 올릴 URL을 한 쌍씩 발급합니다(10분간 유효). "
                    + "올린 뒤 받은 key를 일정 추가·수정 요청의 photos에 실어 보냅니다."
    )
    @PostMapping("/photos/presigned-urls")
    public ResponseEntity<ApiResponse<CalendarPhotoUploadResponse>> createPhotoUploads(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CalendarPhotoUploadRequest request
    ) {
        CalendarPhotoUploadResponse response = calendarService.createPhotoUploads(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success("사진 업로드 URL 발급 성공", response));
    }
}
