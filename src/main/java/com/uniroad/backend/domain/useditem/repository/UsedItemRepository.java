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

    // 정렬은 Pageable의 Sort(id 오름/내림)로 붙는다. oldest는 커서 비교 방향만 정한다.
    @Query("""
        SELECT u
        FROM UsedItemPost u
        WHERE (:cursorId IS NULL OR (:oldest = false AND u.id < :cursorId) OR (:oldest = true AND u.id > :cursorId))
          AND (:title IS NULL OR LOWER(u.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:country IS NULL OR LOWER(u.country) LIKE LOWER(CONCAT('%', :country, '%')))
          AND (:region IS NULL OR LOWER(u.region) LIKE LOWER(CONCAT('%', :region, '%')))
          AND (:content IS NULL OR LOWER(u.content) LIKE LOWER(CONCAT('%', :content, '%')))
          AND (:status IS NULL OR u.status = :status)
          AND (:minPrice IS NULL OR u.price >= :minPrice)
          AND (:maxPrice IS NULL OR u.price <= :maxPrice)
    """)
    List<UsedItemPost> searchByCursor(
            @Param("cursorId") Long cursorId,
            @Param("oldest") boolean oldest,
            @Param("title") String title,
            @Param("country") String country,
            @Param("region") String region,
            @Param("content") String content,
            @Param("status") com.uniroad.backend.domain.useditem.entity.UsedItemStatus status,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
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
