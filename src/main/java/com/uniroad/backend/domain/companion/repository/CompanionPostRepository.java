package com.uniroad.backend.domain.companion.repository;

import com.uniroad.backend.domain.companion.entity.CompanionPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long> {
    List<CompanionPost> findAllByOrderByCreatedAtDesc();
    List<CompanionPost> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

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
}
