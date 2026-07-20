package com.uniroad.backend.domain.useditem.repository;

import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsedItemRepository extends JpaRepository<UsedItemPost, Long> {

    @Query("""
        SELECT u
        FROM UsedItemPost u
        ORDER BY
            CASE WHEN u.region = :userRegion THEN 0 ELSE 1 END,
            u.createdAt DESC
    """)
    List<UsedItemPost> findAllSortedByRegion(@Param("userRegion") String userRegion);

    List<UsedItemPost> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT u
        FROM UsedItemPost u
        WHERE (:cursorId IS NULL OR u.id < :cursorId)
        ORDER BY u.id DESC
    """)
    List<UsedItemPost> findByCursor(
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
        SELECT u
        FROM UsedItemPost u
        WHERE u.author.id = :memberId
          AND (:cursorId IS NULL OR u.id < :cursorId)
        ORDER BY u.id DESC
    """)
    List<UsedItemPost> findByAuthorIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
        SELECT u
        FROM UsedItemPost u
        JOIN Scrap s ON s.targetId = u.id
        WHERE s.member.id = :memberId
          AND s.targetType = :targetType
          AND (:cursorId IS NULL OR u.id < :cursorId)
        ORDER BY s.createdAt DESC, u.id DESC
    """)
    List<UsedItemPost> findScrappedByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("targetType") ScrapTargetType targetType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    void deleteByAuthorId(Long memberId);
}
