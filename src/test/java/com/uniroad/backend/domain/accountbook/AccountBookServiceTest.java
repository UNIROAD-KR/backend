package com.uniroad.backend.domain.accountbook;

import com.uniroad.backend.domain.accountbook.dto.AccountBookRequest;
import com.uniroad.backend.domain.accountbook.dto.AccountBookResponse;
import com.uniroad.backend.domain.accountbook.entity.AccountCategory;
import com.uniroad.backend.domain.accountbook.entity.TransactionType;
import com.uniroad.backend.domain.accountbook.service.AccountBookService;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountBookServiceTest {

    private static final LocalDate SEP_21 = LocalDate.parse("2026-09-21");
    private static final LocalDate SEP_22 = LocalDate.parse("2026-09-22");

    @Autowired
    private AccountBookService accountBookService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager entityManager;

    private Long me;
    private Long other;

    @BeforeEach
    void setUp() {
        me = member("me");
        other = member("other");
    }

    @Test
    @DisplayName("잔액보다 많이 쓰면 거절하지 않고 잔액이 음수가 된다")
    void spendBeyondBalance() {
        add(income("충전", "100"));
        add(expense("월세", "130"));

        assertThat(balance()).isEqualByComparingTo("-30");
        assertThat(accountBookService.getDailyDetails(me, SEP_21)).hasSize(2);
    }

    @Test
    @DisplayName("잔액이 음수여도 충전하면 그만큼 올라간다")
    void chargeFromNegativeBalance() {
        add(expense("점심", "30"));
        add(income("충전", "50"));

        assertThat(balance()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("지출 금액을 고치면 차액만큼 잔액이 바뀌고 내역 내용이 통째로 바뀐다")
    void updateExpenseAmount() {
        add(income("충전", "100"));
        Long lunch = add(expense("점심", "30"));

        AccountBookResponse updated = accountBookService.updateTransaction(me, lunch,
                new AccountBookRequest(new BigDecimal("50"), TransactionType.EXPENSE, AccountCategory.SHOPPING, "장보기", SEP_22));
        sync();

        assertThat(updated.id()).isEqualTo(lunch);
        assertThat(balance()).isEqualByComparingTo("50");
        assertThat(accountBookService.getDailyDetails(me, SEP_21)).extracting(AccountBookResponse::title)
                .containsExactly("충전");
        AccountBookResponse moved = accountBookService.getDailyDetails(me, SEP_22).get(0);
        assertThat(moved.title()).isEqualTo("장보기");
        assertThat(moved.category()).isEqualTo(AccountCategory.SHOPPING);
        assertThat(moved.amount()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("지출을 수입으로 바꾸면 옛 지출을 되돌리고 새 수입을 더한다")
    void updateExpenseToIncome() {
        add(income("충전", "100"));
        Long entry = add(expense("잘못 적은 내역", "30"));

        accountBookService.updateTransaction(me, entry, income("환불", "20"));
        sync();

        assertThat(balance()).isEqualByComparingTo("120");
    }

    @Test
    @DisplayName("이미 쓴 충전도 금액을 늘리는 수정은 받는다")
    void increaseSpentIncome() {
        Long charge = add(income("충전", "100"));
        add(expense("점심", "80"));

        accountBookService.updateTransaction(me, charge, income("충전", "120"));
        sync();

        assertThat(balance()).isEqualByComparingTo("40");
    }

    @Test
    @DisplayName("잔액이 0 미만이 되는 수정도 받고 잔액이 음수가 된다")
    void updateBelowZero() {
        Long charge = add(income("충전", "100"));
        add(expense("점심", "80"));

        accountBookService.updateTransaction(me, charge, income("충전", "50"));
        sync();

        assertThat(balance()).isEqualByComparingTo("-30");
        assertThat(amounts(SEP_21)).usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(new BigDecimal("50"), new BigDecimal("80"));
    }

    @Test
    @DisplayName("지출을 지우면 그 금액이 잔액으로 돌아온다")
    void deleteExpense() {
        add(income("충전", "100"));
        Long lunch = add(expense("점심", "30"));

        accountBookService.deleteTransaction(me, lunch);
        sync();

        assertThat(balance()).isEqualByComparingTo("100");
        assertThat(accountBookService.getDailyDetails(me, SEP_21)).extracting(AccountBookResponse::title)
                .containsExactly("충전");
    }

    @Test
    @DisplayName("충전을 지우면 그 금액이 잔액에서 빠진다")
    void deleteIncome() {
        add(income("충전", "100"));
        Long extra = add(income("추가 충전", "40"));

        accountBookService.deleteTransaction(me, extra);
        sync();

        assertThat(balance()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("잔액이 0 미만이 되는 삭제도 받고 잔액이 음수가 된다")
    void deleteBelowZero() {
        Long charge = add(income("충전", "100"));
        add(expense("점심", "80"));

        accountBookService.deleteTransaction(me, charge);
        sync();

        assertThat(balance()).isEqualByComparingTo("-80");
        assertThat(accountBookService.getDailyDetails(me, SEP_21)).extracting(AccountBookResponse::title)
                .containsExactly("점심");
    }

    @Test
    @DisplayName("남의 내역과 없는 내역은 고치지도 지우지도 못한다")
    void othersEntryIsNotFound() {
        Long mine = add(income("충전", "100"));

        assertError(() -> accountBookService.updateTransaction(other, mine, income("충전", "1")),
                ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
        assertError(() -> accountBookService.deleteTransaction(other, mine), ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
        assertError(() -> accountBookService.deleteTransaction(me, mine + 9999), ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
        sync();

        assertThat(balance()).isEqualByComparingTo("100");
        assertThat(accountBookService.getBalance(other).balance()).isEqualByComparingTo("0");
    }

    // ── 도우미 ────────────────────────────────────────────

    private Long add(AccountBookRequest request) {
        Long id = accountBookService.addTransaction(me, request);
        sync();
        return id;
    }

    private AccountBookRequest income(String title, String amount) {
        return new AccountBookRequest(new BigDecimal(amount), TransactionType.INCOME, AccountCategory.CHARGE, title, SEP_21);
    }

    private AccountBookRequest expense(String title, String amount) {
        return new AccountBookRequest(new BigDecimal(amount), TransactionType.EXPENSE, AccountCategory.FOOD, title, SEP_21);
    }

    private BigDecimal balance() {
        return accountBookService.getBalance(me).balance();
    }

    private List<BigDecimal> amounts(LocalDate date) {
        return accountBookService.getDailyDetails(me, date).stream().map(AccountBookResponse::amount).toList();
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
                .email(key + "@accountbook.test")
                .password("password")
                .name(key)
                .role(Role.VERIFIED)
                .provider("LOCAL")
                .build()).getId();
    }
}
