package com.uniroad.backend.domain.calendar;

import com.uniroad.backend.domain.calendar.dto.CalendarCategoryRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarCategoryResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarDayResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarEventRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarEventResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarEventSearchResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarMonthResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadResponse;
import com.uniroad.backend.domain.calendar.entity.CalendarRepeatType;
import com.uniroad.backend.domain.calendar.event.CalendarPhotoFilesDeletedEvent;
import com.uniroad.backend.domain.calendar.repository.CalendarCategoryRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarDayCoverRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarEventPhotoRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarEventRepository;
import com.uniroad.backend.domain.calendar.service.CalendarCategoryService;
import com.uniroad.backend.domain.calendar.service.CalendarService;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RecordApplicationEvents
class CalendarServiceTest {

    private static final LocalDate SEP_1 = LocalDate.parse("2026-09-01");
    private static final LocalDate SEP_30 = LocalDate.parse("2026-09-30");

    @Autowired
    private CalendarService calendarService;
    @Autowired
    private CalendarCategoryService categoryService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CalendarEventRepository eventRepository;
    @Autowired
    private CalendarEventPhotoRepository photoRepository;
    @Autowired
    private CalendarDayCoverRepository dayCoverRepository;
    @Autowired
    private CalendarCategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ApplicationEvents applicationEvents;

    private Long me;
    private Long other;

    @BeforeEach
    void setUp() {
        me = member("me");
        other = member("other");
    }

    @Test
    @DisplayName("하루 일정의 사진이 여러 장이면 첫 사진이 대표이고, 사진이 없는 날은 대표가 없다")
    void firstPhotoIsCover() {
        CalendarEventResponse dinner = create(day("저녁", "2026-09-21"), "a", "b", "c");
        create(day("사진 없는 날", "2026-09-22"));

        CalendarMonthResponse month = month();

        assertThat(coverByDate(month)).containsOnlyKeys("2026-09-21");
        assertThat(coverByDate(month).get("2026-09-21")).isEqualTo(dinner.photos().get(0).id());
        assertThat(month.events()).extracting(CalendarMonthResponse.Occurrence::title)
                .containsExactly("저녁", "사진 없는 날");
    }

    @Test
    @DisplayName("같은 날 일정이 여럿이면 먼저 등록한 일정의 사진이 대표다")
    void earlierEventWins() {
        // 나중 시각의 일정을 먼저 등록한다. 대표는 일정 시각이 아니라 등록 순서를 따른다.
        CalendarEventResponse registeredFirst = create(timed("저녁", "2026-09-21T19:00", "2026-09-21T20:00"), "a");
        create(timed("아침", "2026-09-21T08:00", "2026-09-21T09:00"), "b");

        assertThat(coverByDate(month()).get("2026-09-21")).isEqualTo(registeredFirst.photos().get(0).id());
    }

    @Test
    @DisplayName("여러 날 일정은 사진을 고른 순서대로 시작일부터 하루에 한 장씩 배정하고, 넘치는 사진은 상세에만 나온다")
    void multiDayPhotosSpreadOverDays() {
        CalendarEventResponse trip = create(allDay("파리 여행", "2026-09-21", "2026-09-23"), "a", "b", "c", "d", "e");
        List<Long> ids = trip.photos().stream().map(CalendarPhotoResponse::id).toList();

        Map<String, Long> covers = coverByDate(month());

        assertThat(covers).containsOnlyKeys("2026-09-21", "2026-09-22", "2026-09-23");
        assertThat(covers.get("2026-09-21")).isEqualTo(ids.get(0));
        assertThat(covers.get("2026-09-22")).isEqualTo(ids.get(1));
        assertThat(covers.get("2026-09-23")).isEqualTo(ids.get(2));

        CalendarDayResponse lastDay = calendarService.getDay(me, LocalDate.parse("2026-09-23"));
        assertThat(lastDay.coverPhotoId()).isEqualTo(ids.get(2));
        assertThat(lastDay.events()).hasSize(1);
        assertThat(lastDay.events().get(0).photos()).hasSize(5);
    }

    @Test
    @DisplayName("사진이 일수보다 적으면 남는 날은 대표 없이 일정만 나온다")
    void multiDayFewerPhotosThanDays() {
        create(allDay("파리 여행", "2026-09-21", "2026-09-23"), "a");

        assertThat(coverByDate(month())).containsOnlyKeys("2026-09-21");

        CalendarDayResponse lastDay = calendarService.getDay(me, LocalDate.parse("2026-09-23"));
        assertThat(lastDay.coverPhotoId()).isNull();
        assertThat(lastDay.events()).extracting(CalendarEventResponse::title).containsExactly("파리 여행");
    }

    @Test
    @DisplayName("조회 범위가 일정 중간에서 시작해도 날짜별 사진 배정은 어긋나지 않는다")
    void multiDayCoverWhenRangeStartsMidEvent() {
        CalendarEventResponse trip = create(allDay("월말 여행", "2026-08-30", "2026-09-02"), "a", "b", "c", "d");

        Map<String, Long> covers = coverByDate(month());

        assertThat(covers).containsOnlyKeys("2026-09-01", "2026-09-02");
        assertThat(covers.get("2026-09-01")).isEqualTo(trip.photos().get(2).id());
        assertThat(covers.get("2026-09-02")).isEqualTo(trip.photos().get(3).id());
    }

    @Test
    @DisplayName("대표 사진을 바꾸면 그 사진이 나오고, 초기화하면 기본 규칙으로 돌아온다")
    void changeAndResetCover() {
        CalendarEventResponse dinner = create(day("저녁", "2026-09-21"), "a", "b");
        Long first = dinner.photos().get(0).id();
        Long second = dinner.photos().get(1).id();
        LocalDate date = LocalDate.parse("2026-09-21");

        calendarService.setDayCover(me, date, second);
        sync();
        assertThat(coverByDate(month()).get("2026-09-21")).isEqualTo(second);

        // 한 번 더 바꿔도 행이 늘지 않고 덮어쓴다.
        calendarService.setDayCover(me, date, first);
        calendarService.setDayCover(me, date, second);
        sync();
        assertThat(dayCoverRepository.count()).isEqualTo(1);

        calendarService.resetDayCover(me, date);
        sync();
        assertThat(coverByDate(month()).get("2026-09-21")).isEqualTo(first);
    }

    @Test
    @DisplayName("그 날짜에 걸치지 않은 사진이나 남의 사진은 대표로 지정할 수 없다")
    void invalidCover() {
        CalendarEventResponse dinner = create(day("저녁", "2026-09-21"), "a");
        Long photoId = dinner.photos().get(0).id();

        assertError(() -> calendarService.setDayCover(me, LocalDate.parse("2026-09-22"), photoId),
                ErrorCode.INVALID_CALENDAR_COVER);
        assertError(() -> calendarService.setDayCover(other, LocalDate.parse("2026-09-21"), photoId),
                ErrorCode.CALENDAR_PHOTO_NOT_FOUND);
    }

    @Test
    @DisplayName("수정에서 빠진 사진은 행과 대표 지정이 지워지고 파일 삭제가 예약되며, 순서를 바꾸면 대표도 따라온다")
    void updatePhotos() {
        CalendarEventResponse dinner = create(day("저녁", "2026-09-21"), "a", "b", "c");
        Long a = dinner.photos().get(0).id();
        Long b = dinner.photos().get(1).id();
        Long c = dinner.photos().get(2).id();
        calendarService.setDayCover(me, LocalDate.parse("2026-09-21"), a);
        sync();

        // a를 빼고, c를 맨 앞으로, 새 사진 d를 맨 뒤에 붙인다.
        CalendarEventResponse updated = calendarService.updateEvent(me, dinner.id(), request(
                "저녁", false, "2026-09-21T19:00", "2026-09-21T20:00", null, null,
                List.of(kept(c), kept(b), added("d"))));
        sync();

        assertThat(updated.photos()).hasSize(3);
        assertThat(updated.photos().get(0).id()).isEqualTo(c);
        assertThat(updated.photos().get(1).id()).isEqualTo(b);
        assertThat(updated.photos().get(2).id()).isNotNull().isNotIn(a, b, c);

        assertThat(photoRepository.findById(a)).isEmpty();
        assertThat(dayCoverRepository.count()).isZero();
        assertThat(coverByDate(month()).get("2026-09-21")).isEqualTo(c);
        assertThat(deletedKeys()).containsExactlyInAnyOrder(key(me, "a"), thumbnailKey(me, "a"));
    }

    @Test
    @DisplayName("일정 날짜를 옮기면 옮기기 전 날짜의 대표 지정은 사라진다")
    void movingEventDropsStaleCover() {
        CalendarEventResponse dinner = create(day("저녁", "2026-09-21"), "a", "b");
        Long b = dinner.photos().get(1).id();
        calendarService.setDayCover(me, LocalDate.parse("2026-09-21"), b);
        sync();

        calendarService.updateEvent(me, dinner.id(), request(
                "저녁", false, "2026-09-25T19:00", "2026-09-25T20:00", null, null,
                dinner.photos().stream().map(photo -> kept(photo.id())).toList()));
        sync();

        assertThat(dayCoverRepository.count()).isZero();
        assertThat(coverByDate(month())).containsOnlyKeys("2026-09-25");
    }

    @Test
    @DisplayName("일정을 지우면 사진과 대표 지정도 지워지고 파일 삭제가 예약된다")
    void deleteEvent() {
        CalendarEventResponse dinner = create(day("저녁", "2026-09-21"), "a");
        calendarService.setDayCover(me, LocalDate.parse("2026-09-21"), dinner.photos().get(0).id());
        sync();

        calendarService.deleteEvent(me, dinner.id());
        sync();

        assertThat(eventRepository.count()).isZero();
        assertThat(photoRepository.count()).isZero();
        assertThat(dayCoverRepository.count()).isZero();
        assertThat(deletedKeys()).containsExactlyInAnyOrder(key(me, "a"), thumbnailKey(me, "a"));
    }

    @Test
    @DisplayName("종료가 시작보다 빠르면 저장할 수 없고, 같으면 저장된다")
    void endBeforeStart() {
        assertError(() -> create(timed("거꾸로", "2026-09-21T10:00", "2026-09-21T09:59")),
                ErrorCode.INVALID_CALENDAR_PERIOD);
        assertError(() -> create(allDay("거꾸로", "2026-09-22", "2026-09-21")),
                ErrorCode.INVALID_CALENDAR_PERIOD);

        assertThat(create(timed("같은 시각", "2026-09-21T10:00", "2026-09-21T10:00")).id()).isNotNull();
    }

    @Test
    @DisplayName("하루 종일 일정은 시각을 버리고 날짜만 저장한다")
    void allDayDropsTime() {
        CalendarEventResponse event = create(request(
                "휴일", true, "2026-09-21T09:00", "2026-09-21T10:00", null, null, List.of()));

        assertThat(event.startAt()).isEqualTo(LocalDateTime.parse("2026-09-21T00:00"));
        assertThat(event.endAt()).isEqualTo(LocalDateTime.parse("2026-09-21T00:00"));
    }

    @Test
    @DisplayName("제목도 사진도 없거나, 사진이 10장을 넘으면 저장할 수 없다")
    void emptyOrTooManyPhotos() {
        assertError(() -> create(day("  ", "2026-09-21")), ErrorCode.CALENDAR_EVENT_EMPTY);
        assertError(() -> create(day("많다", "2026-09-21"),
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"),
                ErrorCode.CALENDAR_PHOTO_LIMIT_EXCEEDED);

        assertThat(create(day(null, "2026-09-21"), "only-photo").title()).isNull();
    }

    @Test
    @DisplayName("남이 올린 파일, 이미 다른 사진이 쓰는 파일, 다른 일정의 사진은 첨부할 수 없다")
    void foreignPhotos() {
        CalendarEventResponse mine = create(day("내 일정", "2026-09-21"), "a");

        assertError(() -> calendarService.createEvent(me, request(
                        "남의 파일", false, "2026-09-22T09:00", "2026-09-22T10:00", null, null,
                        List.of(new CalendarEventRequest.Photo(null, key(other, "x"), null, null, null)))),
                ErrorCode.INVALID_CALENDAR_PHOTO);
        assertError(() -> calendarService.createEvent(me, request(
                        "경로 조작", false, "2026-09-22T09:00", "2026-09-22T10:00", null, null,
                        List.of(new CalendarEventRequest.Photo(
                                null, "private/calendar/" + me + "/../" + other + "/x.jpg", null, null, null)))),
                ErrorCode.INVALID_CALENDAR_PHOTO);
        assertError(() -> create(day("같은 파일", "2026-09-22"), "a"), ErrorCode.INVALID_CALENDAR_PHOTO);

        CalendarEventResponse second = create(day("다른 일정", "2026-09-23"), "b");
        assertError(() -> calendarService.updateEvent(me, second.id(), request(
                        "다른 일정", false, "2026-09-23T09:00", "2026-09-23T10:00", null, null,
                        List.of(kept(mine.photos().get(0).id())))),
                ErrorCode.INVALID_CALENDAR_PHOTO);
    }

    @Test
    @DisplayName("남의 일정은 조회·수정·삭제 모두 없는 일정으로 답하고, 남의 캘린더에 섞여 나오지 않는다")
    void othersEvent() {
        CalendarEventResponse mine = create(day("내 일정", "2026-09-21"), "a");

        assertError(() -> calendarService.getEvent(other, mine.id()), ErrorCode.CALENDAR_EVENT_NOT_FOUND);
        assertError(() -> calendarService.updateEvent(other, mine.id(), day("탈취", "2026-09-21")),
                ErrorCode.CALENDAR_EVENT_NOT_FOUND);
        assertError(() -> calendarService.deleteEvent(other, mine.id()), ErrorCode.CALENDAR_EVENT_NOT_FOUND);

        CalendarMonthResponse othersMonth = calendarService.getMonth(other, SEP_1, SEP_30);
        assertThat(othersMonth.events()).isEmpty();
        assertThat(othersMonth.dayCovers()).isEmpty();
    }

    @Test
    @DisplayName("반복 일정은 회차마다 나오지만 사진은 등록한 원래 날짜에만 대표로 붙는다")
    void repeatingEvent() {
        create(request("수업", false, "2026-09-07T13:00", "2026-09-07T15:00",
                CalendarRepeatType.WEEKLY, LocalDate.parse("2026-09-21"), List.of(added("a"))));

        CalendarMonthResponse month = month();

        assertThat(month.events()).extracting(occurrence -> occurrence.startAt().toString())
                .containsExactly("2026-09-07T13:00", "2026-09-14T13:00", "2026-09-21T13:00");
        assertThat(month.events()).allMatch(CalendarMonthResponse.Occurrence::repeating);
        assertThat(coverByDate(month)).containsOnlyKeys("2026-09-07");

        CalendarEventResponse second = calendarService.getDay(me, LocalDate.parse("2026-09-14")).events().get(0);
        assertThat(second.startAt()).isEqualTo(LocalDateTime.parse("2026-09-07T13:00"));
        assertThat(second.occurrenceStartAt()).isEqualTo(LocalDateTime.parse("2026-09-14T13:00"));
        assertThat(second.occurrenceEndAt()).isEqualTo(LocalDateTime.parse("2026-09-14T15:00"));
    }

    @Test
    @DisplayName("반복 간격보다 긴 일정이나, 시작일보다 이른 반복 종료일은 저장할 수 없다")
    void invalidRepeat() {
        assertError(() -> create(request("겹침", true, "2026-09-21T00:00", "2026-09-23T00:00",
                        CalendarRepeatType.DAILY, null, List.of())),
                ErrorCode.INVALID_CALENDAR_REPEAT);
        assertError(() -> create(request("끝이 먼저", false, "2026-09-21T09:00", "2026-09-21T10:00",
                        CalendarRepeatType.WEEKLY, LocalDate.parse("2026-09-20"), List.of())),
                ErrorCode.INVALID_CALENDAR_REPEAT);
    }

    @Test
    @DisplayName("날짜 상세는 하루 종일 일정을 먼저, 그다음 시작 시각 순으로 돌려준다")
    void dayOrder() {
        create(timed("저녁", "2026-09-21T19:00", "2026-09-21T20:00"));
        create(timed("아침", "2026-09-21T08:00", "2026-09-21T09:00"));
        create(allDay("휴일", "2026-09-21", "2026-09-21"));
        create(timed("전날", "2026-09-20T08:00", "2026-09-20T09:00"));

        assertThat(calendarService.getDay(me, LocalDate.parse("2026-09-21")).events())
                .extracting(CalendarEventResponse::title)
                .containsExactly("휴일", "아침", "저녁");
    }

    @Test
    @DisplayName("조회 기간이 거꾸로이거나 너무 길면 거절한다")
    void invalidRange() {
        assertError(() -> calendarService.getMonth(me, SEP_30, SEP_1), ErrorCode.INVALID_CALENDAR_RANGE);
        assertError(() -> calendarService.getMonth(me, SEP_1, SEP_1.plusDays(100)), ErrorCode.INVALID_CALENDAR_RANGE);

        assertThat(calendarService.getMonth(me, SEP_1, SEP_1.plusDays(99)).events()).isEmpty();
    }

    @Test
    @DisplayName("검색은 제목과 메모에서 찾아 최근 일정부터 이어 읽고, %와 _를 글자 그대로 찾는다")
    void search() {
        create(request("파리 1일차", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null, List.of()));
        create(request("루브르", false, "2026-09-22T09:00", "2026-09-22T10:00", null, null, List.of(), "파리에서 제일 좋았다"));
        create(request("PARIS 마지막", false, "2026-09-23T09:00", "2026-09-23T10:00", null, null, List.of(added("a"))));
        create(request("paris 같은 시각", false, "2026-09-23T09:00", "2026-09-23T10:00", null, null, List.of()));
        create(request("할인 50%", false, "2026-09-24T09:00", "2026-09-24T10:00", null, null, List.of()));
        calendarService.createEvent(other, request(
                "남의 파리", false, "2026-09-25T09:00", "2026-09-25T10:00", null, null, List.of()));
        sync();

        CursorPageResponse<CalendarEventSearchResponse> first = calendarService.search(me, "파리", null, 1);
        assertThat(first.items()).extracting(CalendarEventSearchResponse::title).containsExactly("루브르");
        assertThat(first.hasNext()).isTrue();

        CursorPageResponse<CalendarEventSearchResponse> second =
                calendarService.search(me, "파리", first.nextCursorId(), 10);
        assertThat(second.items()).extracting(CalendarEventSearchResponse::title).containsExactly("파리 1일차");
        assertThat(second.hasNext()).isFalse();
        assertThat(second.nextCursorId()).isNull();

        // 시작 시각이 같은 두 건도 한 건씩 빠짐없이 이어 읽힌다.
        CursorPageResponse<CalendarEventSearchResponse> paris1 = calendarService.search(me, "paris", null, 1);
        CursorPageResponse<CalendarEventSearchResponse> paris2 =
                calendarService.search(me, "paris", paris1.nextCursorId(), 1);
        assertThat(List.of(paris1.items().get(0).title(), paris2.items().get(0).title()))
                .containsExactly("paris 같은 시각", "PARIS 마지막");
        assertThat(paris2.hasNext()).isFalse();
        assertThat(paris2.items().get(0).thumbnailUrl()).contains("a_thumb.jpg");

        assertThat(calendarService.search(me, "%", null, 10).items())
                .extracting(CalendarEventSearchResponse::title).containsExactly("할인 50%");
        assertThat(calendarService.search(me, "_", null, 10).items()).isEmpty();
        assertThat(calendarService.search(me, "  ", null, 10).items()).isEmpty();
    }

    @Test
    @DisplayName("카테고리는 처음 조회할 때 기본값이 생기고, 일정은 카테고리의 색을 따른다")
    void categories() {
        List<CalendarCategoryResponse> defaults = categoryService.getCategories(me);
        assertThat(defaults).extracting(CalendarCategoryResponse::name).containsExactly("수업", "여행", "약속", "기타");
        assertThat(categoryService.getCategories(me)).hasSize(4);

        CalendarCategoryResponse club = categoryService.createCategory(me, new CalendarCategoryRequest(" 동아리 ", "#112233"));
        assertThat(club.name()).isEqualTo("동아리");
        assertThat(categoryService.getCategories(me)).extracting(CalendarCategoryResponse::name).last().isEqualTo("동아리");
        assertError(() -> categoryService.createCategory(me, new CalendarCategoryRequest("동아리", "#445566")),
                ErrorCode.DUPLICATE_CALENDAR_CATEGORY);

        CalendarEventResponse event = create(request(
                "정기 모임", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null, List.of(), null, club.id()));
        assertThat(event.color()).isEqualTo("#112233");
        assertThat(month().events().get(0).color()).isEqualTo("#112233");

        // 남의 카테고리는 쓸 수 없다.
        assertError(() -> calendarService.createEvent(other, request(
                        "탈취", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null, List.of(), null, club.id())),
                ErrorCode.CALENDAR_CATEGORY_NOT_FOUND);
        assertError(() -> categoryService.deleteCategory(other, club.id()), ErrorCode.CALENDAR_CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리를 지워도 일정은 미분류로 남는다")
    void deleteCategoryKeepsEvents() {
        CalendarCategoryResponse club = categoryService.createCategory(me, new CalendarCategoryRequest("동아리", "#112233"));
        CalendarEventResponse event = create(request(
                "정기 모임", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null, List.of(), null, club.id()));

        categoryService.deleteCategory(me, club.id());
        sync();

        CalendarEventResponse found = calendarService.getEvent(me, event.id());
        assertThat(found.categoryId()).isNull();
        assertThat(found.color()).isNull();
        assertThat(categoryRepository.findById(club.id())).isEmpty();
    }

    @Test
    @DisplayName("업로드 URL은 회원별 비공개 경로로 발급되고, 받은 key로 사진을 붙일 수 있다")
    void photoUploads() {
        CalendarPhotoUploadResponse uploads = calendarService.createPhotoUploads(me, new CalendarPhotoUploadRequest(List.of(
                new CalendarPhotoUploadRequest.Item("image/png", "image/jpeg"),
                new CalendarPhotoUploadRequest.Item("image/jpeg", null))));

        CalendarPhotoUploadResponse.Item png = uploads.photos().get(0);
        assertThat(png.key()).startsWith("private/calendar/" + me + "/").endsWith(".png");
        assertThat(png.thumbnailKey()).startsWith("private/calendar/" + me + "/").endsWith("_thumb.jpg");
        assertThat(png.uploadUrl()).contains(png.key()).contains("X-Amz-Signature");
        assertThat(png.thumbnailUploadUrl()).contains(png.thumbnailKey());
        assertThat(uploads.photos().get(1).key()).isNotEqualTo(png.key());

        CalendarEventResponse event = calendarService.createEvent(me, request(
                "업로드", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null,
                List.of(new CalendarEventRequest.Photo(null, png.key(), png.thumbnailKey(), 1200, 900))));
        assertThat(event.photos().get(0).imageUrl()).contains(png.key()).contains("X-Amz-Signature");
        assertThat(event.photos().get(0).thumbnailUrl()).contains(png.thumbnailKey());
        assertThat(event.photos().get(0).width()).isEqualTo(1200);

        assertError(() -> calendarService.createPhotoUploads(me, new CalendarPhotoUploadRequest(List.of(
                        new CalendarPhotoUploadRequest.Item("image/heic", null)))),
                ErrorCode.INVALID_CALENDAR_PHOTO);
    }

    @Test
    @DisplayName("썸네일을 올리지 않은 사진은 원본을 썸네일로 쓴다")
    void thumbnailFallsBackToOriginal() {
        CalendarEventResponse event = calendarService.createEvent(me, request(
                "원본만", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null,
                List.of(new CalendarEventRequest.Photo(null, key(me, "a"), null, null, null))));
        sync();

        assertThat(event.photos().get(0).thumbnailUrl()).contains("/a.jpg");
        assertThat(month().dayCovers().get(0).thumbnailUrl()).contains("/a.jpg");
    }

    @Test
    @DisplayName("탈퇴 정리는 그 회원의 캘린더만 지우고, 그 뒤에는 회원 행을 지울 수 있다")
    void deleteAllByMember() {
        categoryService.getCategories(me);
        CalendarEventResponse mine = create(day("내 일정", "2026-09-21"), "a", "b");
        calendarService.setDayCover(me, LocalDate.parse("2026-09-21"), mine.photos().get(1).id());
        calendarService.createEvent(other, request(
                "남의 일정", false, "2026-09-21T09:00", "2026-09-21T10:00", null, null,
                List.of(new CalendarEventRequest.Photo(null, key(other, "x"), null, null, null))));
        sync();

        calendarService.deleteAllByMember(me);
        sync();
        memberRepository.deleteById(me);
        sync();

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(photoRepository.count()).isEqualTo(1);
        assertThat(dayCoverRepository.count()).isZero();
        assertThat(categoryRepository.count()).isZero();
        assertThat(deletedKeys()).containsExactlyInAnyOrder(
                key(me, "a"), thumbnailKey(me, "a"), key(me, "b"), thumbnailKey(me, "b"));
    }

    // ── 도우미 ──────────────────────────────────────────────

    private CalendarEventResponse create(CalendarEventRequest base, String... photoNames) {
        List<CalendarEventRequest.Photo> photos = new ArrayList<>();
        if (base.photos() != null) {
            photos.addAll(base.photos());
        }
        for (String name : photoNames) {
            photos.add(added(name));
        }

        CalendarEventResponse response = calendarService.createEvent(me, new CalendarEventRequest(
                base.categoryId(), base.title(), base.allDay(), base.startAt(), base.endAt(),
                base.reminder(), base.repeatType(), base.repeatUntil(), base.memo(), photos));
        sync();
        return response;
    }

    private CalendarEventRequest day(String title, String date) {
        return timed(title, date + "T19:00", date + "T20:00");
    }

    private CalendarEventRequest timed(String title, String startAt, String endAt) {
        return request(title, false, startAt, endAt, null, null, List.of());
    }

    private CalendarEventRequest allDay(String title, String startDate, String endDate) {
        return request(title, true, startDate + "T00:00", endDate + "T00:00", null, null, List.of());
    }

    private CalendarEventRequest request(
            String title, boolean allDay, String startAt, String endAt,
            CalendarRepeatType repeatType, LocalDate repeatUntil, List<CalendarEventRequest.Photo> photos
    ) {
        return request(title, allDay, startAt, endAt, repeatType, repeatUntil, photos, null);
    }

    private CalendarEventRequest request(
            String title, boolean allDay, String startAt, String endAt,
            CalendarRepeatType repeatType, LocalDate repeatUntil, List<CalendarEventRequest.Photo> photos, String memo
    ) {
        return request(title, allDay, startAt, endAt, repeatType, repeatUntil, photos, memo, null);
    }

    private CalendarEventRequest request(
            String title, boolean allDay, String startAt, String endAt,
            CalendarRepeatType repeatType, LocalDate repeatUntil, List<CalendarEventRequest.Photo> photos,
            String memo, Long categoryId
    ) {
        return new CalendarEventRequest(
                categoryId, title, allDay, LocalDateTime.parse(startAt), LocalDateTime.parse(endAt),
                null, repeatType, repeatUntil, memo, photos);
    }

    private CalendarEventRequest.Photo added(String name) {
        return new CalendarEventRequest.Photo(null, key(me, name), thumbnailKey(me, name), null, null);
    }

    private CalendarEventRequest.Photo kept(Long photoId) {
        return new CalendarEventRequest.Photo(photoId, null, null, null, null);
    }

    private String key(Long memberId, String name) {
        return "private/calendar/" + memberId + "/" + name + ".jpg";
    }

    private String thumbnailKey(Long memberId, String name) {
        return "private/calendar/" + memberId + "/" + name + "_thumb.jpg";
    }

    private CalendarMonthResponse month() {
        return calendarService.getMonth(me, SEP_1, SEP_30);
    }

    private Map<String, Long> coverByDate(CalendarMonthResponse month) {
        return month.dayCovers().stream()
                .collect(Collectors.toMap(cover -> cover.date().toString(), CalendarMonthResponse.DayCover::photoId));
    }

    private List<String> deletedKeys() {
        return applicationEvents.stream(CalendarPhotoFilesDeletedEvent.class)
                .flatMap(event -> event.keys().stream())
                .toList();
    }

    private void assertError(ThrowingCallable call, ErrorCode expected) {
        assertThatThrownBy(call)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getCode())
                .isEqualTo(expected.name());
    }

    /** 서비스가 실제 요청처럼 DB에서 다시 읽도록, 쓴 내용을 내려보내고 1차 캐시를 비운다. */
    private void sync() {
        entityManager.flush();
        entityManager.clear();
    }

    private Long member(String key) {
        return memberRepository.save(Member.builder()
                .email(key + "@calendar.test")
                .password("password")
                .name(key)
                .role(Role.VERIFIED)
                .provider("LOCAL")
                .build()).getId();
    }
}
