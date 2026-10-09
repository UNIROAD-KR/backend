package com.uniroad.backend.domain.calendar.repository;

import com.uniroad.backend.domain.calendar.entity.CalendarEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("""
            SELECT e
            FROM CalendarEvent e
            LEFT JOIN FETCH e.category
            WHERE e.id = :id
              AND e.member.id = :memberId
            """)
    Optional<CalendarEvent> findByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

    /** 반복하지 않는 일정 중 조회 범위와 하루라도 겹치는 것. */
    @Query("""
            SELECT e
            FROM CalendarEvent e
            LEFT JOIN FETCH e.category
            WHERE e.member.id = :memberId
              AND e.repeatType = com.uniroad.backend.domain.calendar.entity.CalendarRepeatType.NONE
              AND e.startAt < :toExclusive
              AND e.endAt >= :from
            """)
    List<CalendarEvent> findSingleOverlapping(
            @Param("memberId") Long memberId,
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    /**
     * 반복 일정 중 조회 범위가 끝나기 전에 시작한 것.
     *
     * 어느 회차가 범위에 걸리는지는 SQL로 가리기 어려워 후보만 가져오고 서비스에서 펼친다.
     * 반복 일정은 회원당 몇 건 되지 않는다.
     */
    @Query("""
            SELECT e
            FROM CalendarEvent e
            LEFT JOIN FETCH e.category
            WHERE e.member.id = :memberId
              AND e.repeatType <> com.uniroad.backend.domain.calendar.entity.CalendarRepeatType.NONE
              AND e.startAt < :toExclusive
            """)
    List<CalendarEvent> findRepeatingStartedBefore(
            @Param("memberId") Long memberId,
            @Param("toExclusive") LocalDateTime toExclusive
    );

    // 시작 시각이 같은 일정이 있을 수 있어 (시작 시각, id) 순으로 이어 읽는다.
    // keyword는 서비스에서 LIKE 특수문자를 '!'로 이스케이프해 넘긴다.
    @Query("""
            SELECT e
            FROM CalendarEvent e
            LEFT JOIN FETCH e.category
            WHERE e.member.id = :memberId
              AND (
                    LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
                    OR LOWER(e.memo) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '!'
              )
              AND (
                    :cursorId IS NULL
                    OR e.startAt < :cursorStartAt
                    OR (e.startAt = :cursorStartAt AND e.id < :cursorId)
              )
            ORDER BY e.startAt DESC, e.id DESC
            """)
    List<CalendarEvent> searchByCursor(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            @Param("cursorId") Long cursorId,
            @Param("cursorStartAt") LocalDateTime cursorStartAt,
            Pageable pageable
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CalendarEvent e SET e.category = NULL WHERE e.category.id = :categoryId")
    void clearCategory(@Param("categoryId") Long categoryId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CalendarEvent e WHERE e.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
