package com.uniroad.backend.domain.calendar.repository;

import com.uniroad.backend.domain.calendar.entity.CalendarDayCover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CalendarDayCoverRepository extends JpaRepository<CalendarDayCover, Long> {

    List<CalendarDayCover> findByMemberIdAndCoverDateBetween(Long memberId, LocalDate from, LocalDate to);

    Optional<CalendarDayCover> findByMemberIdAndCoverDate(Long memberId, LocalDate coverDate);

    // 아래 삭제들은 사진 행을 지우기 전에 먼저 실행해야 한다(photo_id FK).
    // 영속성 컨텍스트를 비우지 않는다 — 호출하는 쪽이 수정 중인 일정 엔티티를 계속 들고 있다.
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CalendarDayCover c WHERE c.photo.id IN :photoIds")
    void deleteByPhotoIdIn(@Param("photoIds") Collection<Long> photoIds);

    /** 일정 날짜가 바뀌어 더 이상 그 날짜에 걸치지 않는 대표 사진 지정을 지운다. */
    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM CalendarDayCover c
            WHERE c.photo.id IN :photoIds
              AND (c.coverDate < :startDate OR c.coverDate > :endDate)
            """)
    void deleteByPhotoIdInAndCoverDateOutside(
            @Param("photoIds") Collection<Long> photoIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CalendarDayCover c WHERE c.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
