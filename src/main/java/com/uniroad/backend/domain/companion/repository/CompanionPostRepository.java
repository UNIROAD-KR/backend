package com.uniroad.backend.domain.companion.repository;

import com.uniroad.backend.domain.companion.entity.CompanionPost;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long> {
    List<CompanionPost> findAllByOrderByCreatedAtDesc();
    List<CompanionPost> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    void deleteByMemberId(Long memberId);

    @Query("""
            SELECT c
            FROM CompanionPost c
            WHERE (:cursorId IS NULL OR c.id < :cursorId)
            ORDER BY c.id DESC
            """)
    List<CompanionPost> findByCursor(
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM CompanionPost c
            WHERE c.member.id = :memberId
              AND (:cursorId IS NULL OR c.id < :cursorId)
            ORDER BY c.id DESC
            """)
    List<CompanionPost> findByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM CompanionPost c
            WHERE (:cursorId IS NULL OR c.id < :cursorId)
              AND (:status IS NULL OR c.status = :status)
              AND (:country IS NULL OR LOWER(c.country) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (:region IS NULL OR LOWER(c.region) LIKE LOWER(CONCAT('%', :region, '%')))
              AND (:startDateFrom IS NULL OR c.startDate >= :startDateFrom)
              AND (:startDateTo IS NULL OR c.startDate <= :startDateTo)
              AND (:endDateFrom IS NULL OR c.endDate >= :endDateFrom)
              AND (:endDateTo IS NULL OR c.endDate <= :endDateTo)
            ORDER BY c.id DESC
            """)
    List<CompanionPost> searchByCursor(
            @Param("cursorId") Long cursorId,
            @Param("status") com.uniroad.backend.domain.companion.entity.RecruitmentStatus status,
            @Param("country") String country,
            @Param("region") String region,
            @Param("startDateFrom") java.time.LocalDate startDateFrom,
            @Param("startDateTo") java.time.LocalDate startDateTo,
            @Param("endDateFrom") java.time.LocalDate endDateFrom,
            @Param("endDateTo") java.time.LocalDate endDateTo,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM CompanionPost c
            JOIN Scrap s ON s.targetId = c.id
            WHERE s.member.id = :memberId
              AND s.targetType = :targetType
              AND (:cursorId IS NULL OR c.id < :cursorId)
            ORDER BY s.createdAt DESC, c.id DESC
            """)
    List<CompanionPost> findScrappedByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("targetType") ScrapTargetType targetType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
