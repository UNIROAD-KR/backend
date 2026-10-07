package com.uniroad.backend.domain.filter;

import com.uniroad.backend.domain.community.freepost.entity.FreePost;
import com.uniroad.backend.domain.community.freepost.entity.FreePostCategory;
import com.uniroad.backend.domain.community.freepost.entity.FreePostLike;
import com.uniroad.backend.domain.community.freepost.repository.FreePostLikeRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostRepository;
import com.uniroad.backend.domain.companion.entity.CompanionPost;
import com.uniroad.backend.domain.companion.entity.GenderCondition;
import com.uniroad.backend.domain.companion.entity.RecruitmentStatus;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import com.uniroad.backend.domain.ticket.entity.TicketType;
import com.uniroad.backend.domain.ticket.repository.TicketTransferRepository;
import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import com.uniroad.backend.global.common.SortOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@org.springframework.context.annotation.Import(com.uniroad.backend.global.config.JpaConfig.class)
@ActiveProfiles("test")
class PostFilterQueryTest {

    private static final Pageable PAGE = PageRequest.of(0, 10);
    private static final Pageable LATEST = PageRequest.of(0, 10, SortOrder.LATEST.byId());
    private static final Pageable OLDEST = PageRequest.of(0, 2, SortOrder.OLDEST.byId());

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UsedItemRepository usedItemRepository;
    @Autowired
    private TicketTransferRepository ticketTransferRepository;
    @Autowired
    private CompanionPostRepository companionPostRepository;
    @Autowired
    private FreePostRepository freePostRepository;
    @Autowired
    private FreePostLikeRepository freePostLikeRepository;

    @Test
    @DisplayName("중고거래는 가격 범위로도, 한쪽만 제한해서도 조회된다")
    void usedItemPriceRange() {
        Member author = member("used");
        usedItem(author, "1만", 10_000L);
        usedItem(author, "3만", 30_000L);
        usedItem(author, "5만", 50_000L);

        assertThat(usedItemTitles(20_000L, 40_000L)).containsExactly("3만");
        assertThat(usedItemTitles(30_000L, null)).containsExactly("5만", "3만");
        assertThat(usedItemTitles(null, 30_000L)).containsExactly("3만", "1만");
        assertThat(usedItemTitles(null, null)).hasSize(3);
    }

    @Test
    @DisplayName("티켓은 가격 범위와 이용 마감일 이전 조건으로 조회된다")
    void ticketPriceAndUseDate() {
        Member author = member("ticket");
        ticketTransferRepository.save(ticket(author, TicketType.TOUR, "투어", 20L).useDate("2026-10-10").build());
        ticketTransferRepository.save(ticket(author, TicketType.CONCERT, "공연", 50L).performanceDate("2026-10-20").build());
        ticketTransferRepository.save(ticket(author, TicketType.TRAIN, "기차", 80L).departureDate("2026-11-01").build());
        // 숙박은 체크인이 아니라 체크아웃일이 이용 마감일이다.
        ticketTransferRepository.save(ticket(author, TicketType.ACCOMMODATION, "숙박", 100L)
                .checkInDate("2026-10-18").checkOutDate("2026-10-25").build());
        ticketTransferRepository.save(ticket(author, TicketType.ETC, "날짜없음", 10L).build());

        assertThat(ticketTitles(null, null, "2026-10-20")).containsExactly("공연", "투어");
        assertThat(ticketTitles(null, null, "2026-10-25")).containsExactly("숙박", "공연", "투어");
        assertThat(ticketTitles(30L, 90L, null)).containsExactly("기차", "공연");
        assertThat(ticketTitles(30L, null, "2026-10-31")).containsExactly("숙박", "공연");
        assertThat(ticketTitles(null, null, null)).hasSize(5);
    }

    @Test
    @DisplayName("동행은 성별 조건과 정원 범위로 조회된다")
    void companionGenderAndCapacity() {
        Member author = member("companion");
        companionPostRepository.save(companion(author, "무관4", 4).build());
        companionPostRepository.save(companion(author, "여성2", 2).genderCondition(GenderCondition.FEMALE_ONLY).build());
        companionPostRepository.save(companion(author, "남성6", 6).genderCondition(GenderCondition.MALE_ONLY).build());

        assertThat(companionTitles(GenderCondition.FEMALE_ONLY, null, null)).containsExactly("여성2");
        assertThat(companionTitles(GenderCondition.ANY, null, null)).containsExactly("무관4");
        assertThat(companionTitles(null, null, 4)).containsExactly("여성2", "무관4");
        assertThat(companionTitles(null, 4, null)).containsExactly("남성6", "무관4");
        assertThat(companionTitles(GenderCondition.MALE_ONLY, null, 4)).isEmpty();
        assertThat(companionTitles(null, null, null)).hasSize(3);
    }

    @Test
    @DisplayName("자유게시판은 카테고리별로, 최신순·오래된순·인기순으로 조회된다")
    void freePostCategoryAndSort() {
        Member author = member("free");
        FreePost question = freePost(author, "질문", FreePostCategory.QUESTION);
        FreePost chat = freePost(author, "사담", FreePostCategory.CHAT);
        FreePost worry = freePost(author, "고민", FreePostCategory.WORRY);
        FreePost question2 = freePost(author, "질문2", FreePostCategory.QUESTION);
        like(chat, 3);
        like(question, 1);
        like(question2, 1);

        assertThat(titles(freePostRepository.findLatestByCursor(null, null, null, null, PAGE)))
                .containsExactly("질문2", "고민", "사담", "질문");
        assertThat(titles(freePostRepository.findLatestByCursor(null, null, null, FreePostCategory.QUESTION, PAGE)))
                .containsExactly("질문2", "질문");
        assertThat(titles(freePostRepository.findLatestByCursor(null, null, null, FreePostCategory.WORRY, PAGE)))
                .containsExactly("고민");
        assertThat(titles(freePostRepository.findLatestByCursor(null, "사담", "파견 전", FreePostCategory.CHAT, PAGE)))
                .containsExactly("사담");

        assertThat(titles(freePostRepository.findOldestByCursor(null, null, null, null, PAGE)))
                .containsExactly("질문", "사담", "고민", "질문2");
        assertThat(titles(freePostRepository.findOldestByCursor(chat.getId(), null, null, null, PAGE)))
                .containsExactly("고민", "질문2");

        assertThat(titles(freePostRepository.findPopularByCursor(null, 0L, null, null, null, PAGE)))
                .containsExactly("사담", "질문2", "질문", "고민");
        // 커서(질문2, 좋아요 1) 다음 페이지: 좋아요가 같으면 id가 더 작은 글, 그다음 좋아요가 더 적은 글
        assertThat(titles(freePostRepository.findPopularByCursor(question2.getId(), 1L, null, null, null, PAGE)))
                .containsExactly("질문", "고민");
        assertThat(titles(freePostRepository.findPopularByCursor(null, 0L, null, null, FreePostCategory.QUESTION, PAGE)))
                .containsExactly("질문2", "질문");
        assertThat(worry.getCategory()).isEqualTo(FreePostCategory.WORRY);
    }

    @Test
    @DisplayName("중고거래·티켓·동행 검색은 오래된순으로도 커서를 이어 조회된다")
    void oldestOrder() {
        Member author = member("oldest");
        for (String title : List.of("a", "b", "c")) {
            usedItem(author, title, 100L);
            ticketTransferRepository.save(ticket(author, TicketType.ETC, title, 100L).build());
            companionPostRepository.save(companion(author, title, 4).build());
        }

        List<UsedItemPost> items = usedItemRepository.searchByCursor(null, true, null, null, null, null, null, null, null, OLDEST);
        assertThat(items).extracting(UsedItemPost::getTitle).containsExactly("a", "b");
        assertThat(usedItemRepository.searchByCursor(items.get(1).getId(), true, null, null, null, null, null, null, null, OLDEST))
                .extracting(UsedItemPost::getTitle).containsExactly("c");

        List<TicketTransferPost> tickets = ticketTransferRepository.searchByCursor(
                null, true, null, null, null, null, null, null, null, null, OLDEST);
        assertThat(tickets).extracting(TicketTransferPost::getTitle).containsExactly("a", "b");
        assertThat(ticketTransferRepository.searchByCursor(
                tickets.get(1).getId(), true, null, null, null, null, null, null, null, null, OLDEST))
                .extracting(TicketTransferPost::getTitle).containsExactly("c");

        List<CompanionPost> companions = companionPostRepository.searchByCursor(
                null, true, null, null, null, null, null, null, null, null, null, null, OLDEST);
        assertThat(companions).extracting(CompanionPost::getTitle).containsExactly("a", "b");
        assertThat(companionPostRepository.searchByCursor(
                companions.get(1).getId(), true, null, null, null, null, null, null, null, null, null, null, OLDEST))
                .extracting(CompanionPost::getTitle).containsExactly("c");
    }

    private List<String> usedItemTitles(Long minPrice, Long maxPrice) {
        return usedItemRepository.searchByCursor(null, false, null, null, null, null, null, minPrice, maxPrice, LATEST)
                .stream().map(UsedItemPost::getTitle).toList();
    }

    private List<String> ticketTitles(Long minPrice, Long maxPrice, String useDateTo) {
        return ticketTransferRepository.searchByCursor(null, false, null, null, null, null, null, minPrice, maxPrice, useDateTo, LATEST)
                .stream().map(TicketTransferPost::getTitle).toList();
    }

    private List<String> companionTitles(GenderCondition genderCondition, Integer minCapacity, Integer maxCapacity) {
        return companionPostRepository.searchByCursor(
                        null, false, null, null, null, null, null, null, null, genderCondition, minCapacity, maxCapacity, LATEST)
                .stream().map(CompanionPost::getTitle).toList();
    }

    private List<String> titles(List<FreePost> posts) {
        return posts.stream().map(FreePost::getTitle).toList();
    }

    private Member member(String key) {
        return memberRepository.save(Member.builder()
                .email(key + "@test.com")
                .password("password")
                .name(key)
                .role(Role.VERIFIED)
                .provider("LOCAL")
                .build());
    }

    private void usedItem(Member author, String title, Long price) {
        usedItemRepository.save(UsedItemPost.builder()
                .author(author).title(title).content("내용").price(price).region("파리").semester("2026-2").build());
    }

    private TicketTransferPost.TicketTransferPostBuilder ticket(Member author, TicketType type, String title, Long price) {
        return TicketTransferPost.builder()
                .author(author).ticketType(type).title(title).quantity(1).transferPrice(price);
    }

    private CompanionPost.CompanionPostBuilder companion(Member author, String title, int capacity) {
        return CompanionPost.builder()
                .member(author).title(title).content("내용")
                .startDate(LocalDate.of(2026, 11, 1)).endDate(LocalDate.of(2026, 11, 3))
                .country("프랑스").region("파리").chatLink("https://open.kakao.com/o/test")
                .status(RecruitmentStatus.RECRUITING).capacity(capacity).currentParticipants(1);
    }

    private FreePost freePost(Member author, String title, FreePostCategory category) {
        return freePostRepository.save(FreePost.builder()
                .member(author).title(title).content("내용").country("프랑스").status("파견 전").category(category).build());
    }

    private void like(FreePost post, int count) {
        for (int i = 0; i < count; i++) {
            freePostLikeRepository.save(FreePostLike.builder()
                    .freePost(post).member(member("like-" + post.getId() + "-" + i)).build());
        }
    }
}
