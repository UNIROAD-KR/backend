package com.uniroad.backend.domain.calendar.repository;

import com.uniroad.backend.domain.calendar.entity.CalendarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalendarCategoryRepository extends JpaRepository<CalendarCategory, Long> {

    List<CalendarCategory> findByMemberIdOrderBySortOrderAscIdAsc(Long memberId);

    Optional<CalendarCategory> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByMemberId(Long memberId);

    boolean existsByMemberIdAndName(Long memberId, String name);

    long countByMemberId(Long memberId);

    @Query("SELECT COALESCE(MAX(c.sortOrder), -1) FROM CalendarCategory c WHERE c.member.id = :memberId")
    int findMaxSortOrder(@Param("memberId") Long memberId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CalendarCategory c WHERE c.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
