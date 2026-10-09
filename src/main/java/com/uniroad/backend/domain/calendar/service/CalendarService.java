package com.uniroad.backend.domain.calendar.service;

import com.uniroad.backend.domain.calendar.dto.CalendarDayResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarEventRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarEventResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarEventSearchResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarMonthResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadResponse;
import com.uniroad.backend.domain.calendar.entity.CalendarCategory;
import com.uniroad.backend.domain.calendar.entity.CalendarDayCover;
import com.uniroad.backend.domain.calendar.entity.CalendarEvent;
import com.uniroad.backend.domain.calendar.entity.CalendarEventPhoto;
import com.uniroad.backend.domain.calendar.entity.CalendarReminder;
import com.uniroad.backend.domain.calendar.entity.CalendarRepeatType;
import com.uniroad.backend.domain.calendar.event.CalendarPhotoFilesDeletedEvent;
import com.uniroad.backend.domain.calendar.repository.CalendarCategoryRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarDayCoverRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarEventPhotoRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarEventRepository;
import com.uniroad.backend.domain.calendar.service.CalendarOccurrenceExpander.Occurrence;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private static final int MAX_PHOTOS = 10;
    // 월 그리드는 최대 6주(42일)다. 앱이 앞뒤 달을 미리 받아 두는 경우까지 넉넉히 허용한다.
    private static final int MAX_RANGE_DAYS = 100;
    private static final int DEFAULT_SEARCH_SIZE = 20;
    private static final int MAX_SEARCH_SIZE = 50;

    /**
     * 한 날짜에 사진이 여러 장일 때 대표를 고르는 순서: 먼저 등록한 일정의, 사용자가 앞에 둔 사진.
     *
     * 사진 행의 등록 시각이 아니라 일정의 등록 시각을 본다. 사진 행의 시각으로 고르면
     * 사용자가 일정 안에서 사진 순서를 바꿔도 대표가 따라오지 않는다.
     */
    private static final Comparator<CalendarEventPhoto> COVER_ORDER = Comparator
            .comparing((CalendarEventPhoto photo) -> photo.getEvent().getCreatedAt())
            .thenComparing(photo -> photo.getEvent().getId())
            .thenComparing(CalendarEventPhoto::getSortOrder)
            .thenComparing(CalendarEventPhoto::getId);

    private static final Comparator<Occurrence> DAY_ORDER = Comparator
            .comparing((Occurrence occurrence) -> !occurrence.event().isAllDay())
            .thenComparing(Occurrence::startAt)
            .thenComparing(occurrence -> occurrence.event().getId());

    private final CalendarEventRepository eventRepository;
    private final CalendarEventPhotoRepository photoRepository;
    private final CalendarDayCoverRepository dayCoverRepository;
    private final CalendarCategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final CalendarPhotoStorage photoStorage;
    private final ApplicationEventPublisher eventPublisher;

    // ── 조회 ────────────────────────────────────────────────

    /** 캘린더 메인. from과 to는 둘 다 포함한다. */
    public CalendarMonthResponse getMonth(Long memberId, LocalDate from, LocalDate to) {
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new CustomException(ErrorCode.INVALID_CALENDAR_RANGE);
        }

        RangeData range = loadRange(memberId, from, to);

        List<CalendarMonthResponse.Occurrence> events = range.occurrences().stream()
                .sorted(Comparator.comparing(Occurrence::startAt).thenComparing(o -> o.event().getId()))
                .map(occurrence -> {
                    CalendarEvent event = occurrence.event();
                    CalendarCategory category = event.getCategory();
                    return new CalendarMonthResponse.Occurrence(
                            event.getId(),
                            event.getTitle(),
                            category != null ? category.getId() : null,
                            category != null ? category.getColor() : null,
                            event.isAllDay(),
                            occurrence.startAt(),
                            occurrence.endAt(),
                            event.getRepeatType().repeats(),
                            range.photosOf(event).size()
                    );
                })
                .toList();

        List<CalendarMonthResponse.DayCover> dayCovers = range.covers().entrySet().stream()
                .map(entry -> new CalendarMonthResponse.DayCover(
                        entry.getKey(),
                        entry.getValue().getId(),
                        entry.getValue().getEvent().getId(),
                        photoStorage.readUrl(entry.getValue().getThumbnailKeyOrImageKey())
                ))
                .toList();

        return new CalendarMonthResponse(events, dayCovers);
    }

    /** 선택한 날짜의 상세. 그날에 걸친 일정과 그 사진 전체. */
    public CalendarDayResponse getDay(Long memberId, LocalDate date) {
        RangeData range = loadRange(memberId, date, date);

        List<CalendarEventResponse> events = range.occurrences().stream()
                .sorted(DAY_ORDER)
                .map(occurrence -> toResponse(
                        occurrence.event(),
                        range.photosOf(occurrence.event()),
                        occurrence.startAt(),
                        occurrence.endAt()))
                .toList();

        CalendarEventPhoto cover = range.covers().get(date);
        return new CalendarDayResponse(date, cover != null ? cover.getId() : null, events);
    }

    public CalendarEventResponse getEvent(Long memberId, Long eventId) {
        CalendarEvent event = findEvent(memberId, eventId);
        return toResponse(event, event.getPhotos(), event.getStartAt(), event.getEndAt());
    }

    /** 제목과 메모에서 찾는다. 최근 일정부터 돌려준다. */
    public CursorPageResponse<CalendarEventSearchResponse> search(
            Long memberId, String keyword, Long cursorId, Integer size
    ) {
        if (!StringUtils.hasText(keyword)) {
            return new CursorPageResponse<>(List.of(), null, false);
        }

        int pageSize = size == null ? DEFAULT_SEARCH_SIZE : Math.min(Math.max(size, 1), MAX_SEARCH_SIZE);

        LocalDateTime cursorStartAt = null;
        if (cursorId != null) {
            cursorStartAt = eventRepository.findByIdAndMemberId(cursorId, memberId)
                    .map(CalendarEvent::getStartAt)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE));
        }

        // 다음 페이지가 있는지 알기 위해 한 건 더 읽는다.
        List<CalendarEvent> found = eventRepository.searchByCursor(
                memberId, escapeLike(keyword.trim()), cursorId, cursorStartAt, PageRequest.of(0, pageSize + 1));

        boolean hasNext = found.size() > pageSize;
        List<CalendarEvent> page = hasNext ? found.subList(0, pageSize) : found;
        Map<Long, List<CalendarEventPhoto>> photosByEventId = loadPhotos(page);

        List<CalendarEventSearchResponse> items = page.stream()
                .map(event -> {
                    CalendarCategory category = event.getCategory();
                    List<CalendarEventPhoto> photos = photosByEventId.getOrDefault(event.getId(), List.of());
                    return new CalendarEventSearchResponse(
                            event.getId(),
                            event.getTitle(),
                            event.getMemo(),
                            category != null ? category.getId() : null,
                            category != null ? category.getName() : null,
                            category != null ? category.getColor() : null,
                            event.isAllDay(),
                            event.getStartAt(),
                            event.getEndAt(),
                            photos.isEmpty() ? null : photoStorage.readUrl(photos.get(0).getThumbnailKeyOrImageKey())
                    );
                })
                .toList();

        Long nextCursorId = hasNext ? page.get(page.size() - 1).getId() : null;
        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }

    // ── 일정 ────────────────────────────────────────────────

    @Transactional
    public CalendarEventResponse createEvent(Long memberId, CalendarEventRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        EventValues values = validate(memberId, request);

        CalendarEvent event = CalendarEvent.builder()
                .member(member)
                .category(values.category())
                .title(values.title())
                .allDay(request.allDay())
                .startAt(values.startAt())
                .endAt(values.endAt())
                .reminder(values.reminder())
                .repeatType(values.repeatType())
                .repeatUntil(values.repeatUntil())
                .memo(values.memo())
                .build();

        applyPhotos(event, member, values.photos());
        eventRepository.save(event);

        return toResponse(event, event.getPhotos(), event.getStartAt(), event.getEndAt());
    }

    @Transactional
    public CalendarEventResponse updateEvent(Long memberId, Long eventId, CalendarEventRequest request) {
        CalendarEvent event = findEvent(memberId, eventId);
        EventValues values = validate(memberId, request);

        event.update(
                values.category(),
                values.title(),
                request.allDay(),
                values.startAt(),
                values.endAt(),
                values.reminder(),
                values.repeatType(),
                values.repeatUntil(),
                values.memo()
        );
        applyPhotos(event, event.getMember(), values.photos());

        // 날짜를 옮기면, 옮기기 전 날짜에 지정해 둔 대표 사진은 더 이상 그 날짜의 사진이 아니다.
        List<Long> keptPhotoIds = event.getPhotos().stream()
                .map(CalendarEventPhoto::getId)
                .filter(id -> id != null)
                .toList();
        if (!keptPhotoIds.isEmpty()) {
            dayCoverRepository.deleteByPhotoIdInAndCoverDateOutside(
                    keptPhotoIds, event.getStartDate(), event.getEndDate());
        }

        // 새 사진이 ID를 받아야 응답에 실을 수 있다.
        eventRepository.flush();
        return toResponse(event, event.getPhotos(), event.getStartAt(), event.getEndAt());
    }

    @Transactional
    public void deleteEvent(Long memberId, Long eventId) {
        CalendarEvent event = findEvent(memberId, eventId);
        List<CalendarEventPhoto> photos = List.copyOf(event.getPhotos());

        removeCoversOf(photos);
        eventRepository.delete(event);
        publishFilesDeleted(photos);
    }

    // ── 대표 사진 ────────────────────────────────────────────

    @Transactional
    public void setDayCover(Long memberId, LocalDate date, Long photoId) {
        CalendarEventPhoto photo = photoRepository.findByIdAndMemberId(photoId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CALENDAR_PHOTO_NOT_FOUND));

        if (!photo.getEvent().covers(date)) {
            throw new CustomException(ErrorCode.INVALID_CALENDAR_COVER);
        }

        dayCoverRepository.findByMemberIdAndCoverDate(memberId, date)
                .ifPresentOrElse(
                        cover -> cover.changePhoto(photo),
                        () -> dayCoverRepository.save(CalendarDayCover.create(photo.getMember(), date, photo))
                );
    }

    /** 직접 고른 대표 사진을 지우고 기본 규칙으로 되돌린다. */
    @Transactional
    public void resetDayCover(Long memberId, LocalDate date) {
        dayCoverRepository.findByMemberIdAndCoverDate(memberId, date)
                .ifPresent(dayCoverRepository::delete);
    }

    // ── 사진 업로드 ──────────────────────────────────────────

    public CalendarPhotoUploadResponse createPhotoUploads(Long memberId, CalendarPhotoUploadRequest request) {
        return new CalendarPhotoUploadResponse(request.photos().stream()
                .map(item -> photoStorage.createUpload(memberId, item))
                .toList());
    }

    // ── 탈퇴 ────────────────────────────────────────────────

    /** 회원의 캘린더 데이터를 전부 지운다. FK 순서대로 대표 사진 → 사진 → 일정 → 카테고리. */
    @Transactional
    public void deleteAllByMember(Long memberId) {
        List<CalendarEventPhoto> photos = photoRepository.findByMemberId(memberId);

        dayCoverRepository.deleteByMemberId(memberId);
        photoRepository.deleteByMemberId(memberId);
        eventRepository.deleteByMemberId(memberId);
        categoryRepository.deleteByMemberId(memberId);

        publishFilesDeleted(photos);
    }

    // ── 내부 ────────────────────────────────────────────────

    /** 조회 범위에 걸친 회차, 그 일정들의 사진, 날짜별 대표 사진. */
    private record RangeData(
            List<Occurrence> occurrences,
            Map<Long, List<CalendarEventPhoto>> photosByEventId,
            Map<LocalDate, CalendarEventPhoto> covers
    ) {
        List<CalendarEventPhoto> photosOf(CalendarEvent event) {
            return photosByEventId.getOrDefault(event.getId(), List.of());
        }
    }

    private RangeData loadRange(Long memberId, LocalDate from, LocalDate to) {
        LocalDateTime fromAt = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        List<CalendarEvent> candidates = new ArrayList<>(
                eventRepository.findSingleOverlapping(memberId, fromAt, toExclusive));
        candidates.addAll(eventRepository.findRepeatingStartedBefore(memberId, toExclusive));

        List<Occurrence> occurrences = candidates.stream()
                .flatMap(event -> CalendarOccurrenceExpander.expand(event, from, to).stream())
                .toList();

        List<CalendarEvent> events = occurrences.stream().map(Occurrence::event).distinct().toList();
        Map<Long, List<CalendarEventPhoto>> photosByEventId = loadPhotos(events);

        return new RangeData(
                occurrences,
                photosByEventId,
                resolveCovers(memberId, from, to, events, photosByEventId)
        );
    }

    private Map<Long, List<CalendarEventPhoto>> loadPhotos(Collection<CalendarEvent> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream().map(CalendarEvent::getId).toList();
        return photoRepository.findByEventIdInOrderBySortOrderAscIdAsc(eventIds).stream()
                .collect(Collectors.groupingBy(
                        photo -> photo.getEvent().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 날짜별 대표 사진을 계산한다.
     *
     * 하루짜리 일정은 사진 전부가 그 날짜의 후보다.
     * 여러 날 일정은 사용자가 고른 순서대로 시작일부터 하루에 한 장씩 배정하고,
     * 일수를 넘는 사진은 어느 날짜의 후보도 되지 않는다(상세에서만 보인다).
     *
     * 사진은 일정을 등록한 원래 날짜에만 붙는다. 반복 회차마다 같은 사진이 대표로 깔리면
     * 날짜별 기록이라는 캘린더의 쓸모가 사라진다.
     */
    private Map<LocalDate, CalendarEventPhoto> resolveCovers(
            Long memberId,
            LocalDate from,
            LocalDate to,
            List<CalendarEvent> events,
            Map<Long, List<CalendarEventPhoto>> photosByEventId
    ) {
        Map<LocalDate, CalendarEventPhoto> covers = new TreeMap<>();
        Map<Long, CalendarEventPhoto> photosById = new HashMap<>();

        for (CalendarEvent event : events) {
            List<CalendarEventPhoto> photos = photosByEventId.getOrDefault(event.getId(), List.of());
            int spanDays = event.getSpanDays();

            for (int i = 0; i < photos.size(); i++) {
                CalendarEventPhoto photo = photos.get(i);
                photosById.put(photo.getId(), photo);

                if (spanDays > 0 && i > spanDays) {
                    continue;
                }
                LocalDate date = spanDays == 0 ? event.getStartDate() : event.getStartDate().plusDays(i);
                if (date.isBefore(from) || date.isAfter(to)) {
                    continue;
                }
                covers.merge(date, photo, (current, candidate) ->
                        COVER_ORDER.compare(candidate, current) < 0 ? candidate : current);
            }
        }

        // 사용자가 직접 고른 대표가 계산한 값보다 우선한다.
        for (CalendarDayCover chosen : dayCoverRepository.findByMemberIdAndCoverDateBetween(memberId, from, to)) {
            CalendarEventPhoto photo = photosById.get(chosen.getPhoto().getId());
            if (photo != null && photo.getEvent().covers(chosen.getCoverDate())) {
                covers.put(chosen.getCoverDate(), photo);
            }
        }

        return covers;
    }

    /** 요청을 검증하고 저장할 값으로 다듬은 결과. */
    private record EventValues(
            CalendarCategory category,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            CalendarReminder reminder,
            CalendarRepeatType repeatType,
            LocalDate repeatUntil,
            String memo,
            List<CalendarEventRequest.Photo> photos
    ) {
    }

    private EventValues validate(Long memberId, CalendarEventRequest request) {
        LocalDateTime startAt = request.allDay() ? request.startAt().toLocalDate().atStartOfDay() : request.startAt();
        LocalDateTime endAt = request.allDay() ? request.endAt().toLocalDate().atStartOfDay() : request.endAt();
        if (endAt.isBefore(startAt)) {
            throw new CustomException(ErrorCode.INVALID_CALENDAR_PERIOD);
        }

        CalendarRepeatType repeatType = request.repeatType() != null ? request.repeatType() : CalendarRepeatType.NONE;
        LocalDate repeatUntil = repeatType.repeats() ? request.repeatUntil() : null;
        if (repeatType.repeats()) {
            if (repeatUntil != null && repeatUntil.isBefore(startAt.toLocalDate())) {
                throw new CustomException(ErrorCode.INVALID_CALENDAR_REPEAT);
            }
            // 일정이 반복 간격보다 길면 회차끼리 겹친다. (예: 사흘짜리 일정을 매일 반복)
            long spanDays = ChronoUnit.DAYS.between(startAt.toLocalDate(), endAt.toLocalDate());
            if (spanDays >= repeatType.minIntervalDays()) {
                throw new CustomException(ErrorCode.INVALID_CALENDAR_REPEAT);
            }
        }

        List<CalendarEventRequest.Photo> photos = request.photos() != null ? request.photos() : List.of();
        if (photos.size() > MAX_PHOTOS) {
            throw new CustomException(ErrorCode.CALENDAR_PHOTO_LIMIT_EXCEEDED);
        }

        String title = StringUtils.hasText(request.title()) ? request.title().trim() : null;
        if (title == null && photos.isEmpty()) {
            throw new CustomException(ErrorCode.CALENDAR_EVENT_EMPTY);
        }

        CalendarCategory category = request.categoryId() == null ? null
                : categoryRepository.findByIdAndMemberId(request.categoryId(), memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.CALENDAR_CATEGORY_NOT_FOUND));

        return new EventValues(
                category,
                title,
                startAt,
                endAt,
                request.reminder() != null ? request.reminder() : CalendarReminder.NONE,
                repeatType,
                repeatUntil,
                StringUtils.hasText(request.memo()) ? request.memo() : null,
                photos
        );
    }

    /**
     * 일정의 사진을 요청한 목록으로 맞춘다. 배열 순서가 사진 순서가 된다.
     * 목록에서 빠진 기존 사진은 행과 파일을 모두 지운다.
     */
    private void applyPhotos(CalendarEvent event, Member member, List<CalendarEventRequest.Photo> requested) {
        Map<Long, CalendarEventPhoto> remaining = event.getPhotos().stream()
                .collect(Collectors.toMap(CalendarEventPhoto::getId, photo -> photo));
        List<CalendarEventPhoto> next = new ArrayList<>();
        Set<String> newKeys = new HashSet<>();

        for (int i = 0; i < requested.size(); i++) {
            CalendarEventRequest.Photo item = requested.get(i);

            if (item.photoId() != null) {
                // 다른 일정의 사진이거나 같은 사진을 두 번 보낸 경우 여기서 걸린다.
                CalendarEventPhoto photo = remaining.remove(item.photoId());
                if (photo == null) {
                    throw new CustomException(ErrorCode.INVALID_CALENDAR_PHOTO);
                }
                photo.changeSortOrder(i);
                next.add(photo);
                continue;
            }

            photoStorage.validateOwnedKey(member.getId(), item.key());
            boolean duplicated = !newKeys.add(item.key());
            if (item.thumbnailKey() != null) {
                photoStorage.validateOwnedKey(member.getId(), item.thumbnailKey());
                duplicated |= !newKeys.add(item.thumbnailKey());
            }
            if (duplicated) {
                throw new CustomException(ErrorCode.INVALID_CALENDAR_PHOTO);
            }

            next.add(CalendarEventPhoto.builder()
                    .event(event)
                    .member(member)
                    .imageKey(item.key())
                    .thumbnailKey(item.thumbnailKey())
                    .sortOrder(i)
                    .width(item.width())
                    .height(item.height())
                    .build());
        }

        // 한 파일을 두 사진 행이 가리키면, 한쪽을 지울 때 다른 쪽의 파일까지 사라진다.
        if (!newKeys.isEmpty() && photoRepository.existsByAnyKeyIn(newKeys)) {
            throw new CustomException(ErrorCode.INVALID_CALENDAR_PHOTO);
        }

        List<CalendarEventPhoto> removed = List.copyOf(remaining.values());
        removeCoversOf(removed);
        event.replacePhotos(next);
        publishFilesDeleted(removed);
    }

    /** 사진 행보다 먼저 지워야 한다(photo_id FK). */
    private void removeCoversOf(List<CalendarEventPhoto> photos) {
        if (!photos.isEmpty()) {
            dayCoverRepository.deleteByPhotoIdIn(photos.stream().map(CalendarEventPhoto::getId).toList());
        }
    }

    private void publishFilesDeleted(List<CalendarEventPhoto> photos) {
        List<String> keys = new ArrayList<>();
        for (CalendarEventPhoto photo : photos) {
            keys.add(photo.getImageKey());
            if (photo.getThumbnailKey() != null) {
                keys.add(photo.getThumbnailKey());
            }
        }
        if (!keys.isEmpty()) {
            eventPublisher.publishEvent(new CalendarPhotoFilesDeletedEvent(keys));
        }
    }

    /** 남의 일정은 "권한 없음"이 아니라 "없음"으로 답한다. 일정이 있다는 사실도 알려주지 않는다. */
    private CalendarEvent findEvent(Long memberId, Long eventId) {
        return eventRepository.findByIdAndMemberId(eventId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CALENDAR_EVENT_NOT_FOUND));
    }

    private CalendarEventResponse toResponse(
            CalendarEvent event,
            List<CalendarEventPhoto> photos,
            LocalDateTime occurrenceStartAt,
            LocalDateTime occurrenceEndAt
    ) {
        CalendarCategory category = event.getCategory();
        return new CalendarEventResponse(
                event.getId(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                category != null ? category.getColor() : null,
                event.getTitle(),
                event.isAllDay(),
                event.getStartAt(),
                event.getEndAt(),
                occurrenceStartAt,
                occurrenceEndAt,
                event.getReminder(),
                event.getRepeatType(),
                event.getRepeatUntil(),
                event.getMemo(),
                photos.stream()
                        .map(photo -> new CalendarPhotoResponse(
                                photo.getId(),
                                photoStorage.readUrl(photo.getImageKey()),
                                photoStorage.readUrl(photo.getThumbnailKeyOrImageKey()),
                                photo.getWidth(),
                                photo.getHeight()))
                        .toList()
        );
    }

    private String escapeLike(String keyword) {
        return keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
