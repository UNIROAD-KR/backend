package com.uniroad.backend.domain.calendar.repository;

import com.uniroad.backend.domain.calendar.entity.CalendarEventPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CalendarEventPhotoRepository extends JpaRepository<CalendarEventPhoto, Long> {

    List<CalendarEventPhoto> findByEventIdInOrderBySortOrderAscIdAsc(Collection<Long> eventIds);

    @Query("""
            SELECT p
            FROM CalendarEventPhoto p
            JOIN FETCH p.event
            WHERE p.id = :id
              AND p.member.id = :memberId
            """)
    Optional<CalendarEventPhoto> findByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

    List<CalendarEventPhoto> findByMemberId(Long memberId);

    @Query("""
            SELECT COUNT(p) > 0
            FROM CalendarEventPhoto p
            WHERE p.imageKey IN :keys
               OR p.thumbnailKey IN :keys
            """)
    boolean existsByAnyKeyIn(@Param("keys") Collection<String> keys);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CalendarEventPhoto p WHERE p.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
