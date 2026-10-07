package com.uniroad.backend.domain.ticket.repository;

import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketTransferRepository extends JpaRepository<TicketTransferPost, Long> {

    @Query("""
            SELECT t
            FROM TicketTransferPost t
            WHERE (:cursorId IS NULL OR t.id < :cursorId)
            ORDER BY t.id DESC
            """)
    List<TicketTransferPost> findByCursor(
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM TicketTransferPost t
            WHERE t.author.id = :memberId
              AND (:cursorId IS NULL OR t.id < :cursorId)
            ORDER BY t.id DESC
            """)
    List<TicketTransferPost> findByAuthorIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 이용일은 종류마다 다른 컬럼에 yyyy-MM-dd 문자열로 들어 있어 하나로 합쳐 문자열로 비교한다.
    // 숙박은 체크아웃일을 이용 마감일로 본다. 날짜가 없는 글은 useDateTo를 주면 빠진다.
    // 정렬은 Pageable의 Sort(id 오름/내림)로 붙는다. oldest는 커서 비교 방향만 정한다.
    @Query("""
            SELECT t
            FROM TicketTransferPost t
            WHERE (:cursorId IS NULL OR (:oldest = false AND t.id < :cursorId) OR (:oldest = true AND t.id > :cursorId))
              AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:country IS NULL OR LOWER(t.country) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (
                  :location IS NULL OR
                  LOWER(COALESCE(t.placeName, '')) LIKE LOWER(CONCAT('%', :location, '%')) OR
                  LOWER(COALESCE(t.performancePlace, '')) LIKE LOWER(CONCAT('%', :location, '%')) OR
                  LOWER(COALESCE(t.departureStation, '')) LIKE LOWER(CONCAT('%', :location, '%')) OR
                  LOWER(COALESCE(t.arrivalStation, '')) LIKE LOWER(CONCAT('%', :location, '%')) OR
                  LOWER(COALESCE(t.departureAirport, '')) LIKE LOWER(CONCAT('%', :location, '%')) OR
                  LOWER(COALESCE(t.accommodationName, '')) LIKE LOWER(CONCAT('%', :location, '%')) OR
                  LOWER(COALESCE(t.customTicketType, '')) LIKE LOWER(CONCAT('%', :location, '%'))
              )
              AND (:content IS NULL OR LOWER(t.content) LIKE LOWER(CONCAT('%', :content, '%')))
              AND (:status IS NULL OR t.status = :status)
              AND (:minPrice IS NULL OR t.transferPrice >= :minPrice)
              AND (:maxPrice IS NULL OR t.transferPrice <= :maxPrice)
              AND (
                  :useDateTo IS NULL OR
                  COALESCE(
                      NULLIF(t.checkOutDate, ''),
                      NULLIF(t.checkInDate, ''),
                      NULLIF(t.useDate, ''),
                      NULLIF(t.performanceDate, ''),
                      NULLIF(t.departureDate, '')
                  ) <= :useDateTo
              )
            """)
    List<TicketTransferPost> searchByCursor(
            @Param("cursorId") Long cursorId,
            @Param("oldest") boolean oldest,
            @Param("title") String title,
            @Param("country") String country,
            @Param("location") String location,
            @Param("content") String content,
            @Param("status") com.uniroad.backend.domain.ticket.entity.TicketTransferStatus status,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("useDateTo") String useDateTo,
            Pageable pageable
    );

    @Query("""
            SELECT t
            FROM TicketTransferPost t
            JOIN Scrap s ON s.targetId = t.id
            WHERE s.member.id = :memberId
              AND s.targetType = :targetType
              AND (:cursorId IS NULL OR t.id < :cursorId)
            ORDER BY s.createdAt DESC, t.id DESC
            """)
    List<TicketTransferPost> findScrappedByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("targetType") ScrapTargetType targetType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    void deleteByAuthorId(Long memberId);
}
