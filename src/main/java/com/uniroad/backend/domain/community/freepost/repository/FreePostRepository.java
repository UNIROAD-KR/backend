package com.uniroad.backend.domain.community.freepost.repository;

import com.uniroad.backend.domain.community.freepost.entity.FreePost;
import com.uniroad.backend.domain.community.freepost.entity.FreePostCategory;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FreePostRepository extends JpaRepository<FreePost, Long> {

    List<FreePost> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT f
            FROM FreePost f
            WHERE (:cursorId IS NULL OR f.id < :cursorId)
              AND (:status IS NULL OR f.status = :status)
              AND (:category IS NULL OR f.category = :category)
              AND (
                    :keyword IS NULL
                    OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY f.id DESC
            """)
    List<FreePost> findLatestByCursor(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("category") FreePostCategory category,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FreePost f
            WHERE (:cursorId IS NULL OR f.id > :cursorId)
              AND (:status IS NULL OR f.status = :status)
              AND (:category IS NULL OR f.category = :category)
              AND (
                    :keyword IS NULL
                    OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY f.id ASC
            """)
    List<FreePost> findOldestByCursor(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("category") FreePostCategory category,
            Pageable pageable
    );

    // 좋아요 수는 컬럼이 아니라 세어야 하므로, 커서 글의 좋아요 수(cursorLikeCount)를 함께 받아 (좋아요 수, id) 순으로 이어 읽는다.
    @Query("""
            SELECT f
            FROM FreePost f
            WHERE (
                    :cursorId IS NULL
                    OR (SELECT COUNT(l) FROM FreePostLike l WHERE l.freePost = f) < :cursorLikeCount
                    OR (
                        (SELECT COUNT(l) FROM FreePostLike l WHERE l.freePost = f) = :cursorLikeCount
                        AND f.id < :cursorId
                    )
              )
              AND (:status IS NULL OR f.status = :status)
              AND (:category IS NULL OR f.category = :category)
              AND (
                    :keyword IS NULL
                    OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY (SELECT COUNT(l) FROM FreePostLike l WHERE l.freePost = f) DESC, f.id DESC
            """)
    List<FreePost> findPopularByCursor(
            @Param("cursorId") Long cursorId,
            @Param("cursorLikeCount") long cursorLikeCount,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("category") FreePostCategory category,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FreePost f
            WHERE (:cursorId IS NULL OR f.id < :cursorId)
              AND f.status = :status
            ORDER BY f.id DESC
            """)
    List<FreePost> findByCursorAndStatus(
            @Param("cursorId") Long cursorId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FreePost f
            WHERE (:cursorId IS NULL OR f.id < :cursorId)
              AND (:title IS NULL OR LOWER(f.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:content IS NULL OR LOWER(f.content) LIKE LOWER(CONCAT('%', :content, '%')))
            ORDER BY f.id DESC
            """)
    List<FreePost> searchByCursor(
            @Param("cursorId") Long cursorId,
            @Param("title") String title,
            @Param("content") String content,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FreePost f
            WHERE f.member.id = :memberId
              AND (:cursorId IS NULL OR f.id < :cursorId)
            ORDER BY f.id DESC
            """)
    List<FreePost> findByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FreePost f
            JOIN FreePostLike l ON l.freePost = f
            WHERE l.member.id = :memberId
              AND (:cursorId IS NULL OR f.id < :cursorId)
            ORDER BY f.id DESC
            """)
    List<FreePost> findLikedByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM FreePost f
            LEFT JOIN FreePostLike l ON l.freePost = f
            GROUP BY f
            ORDER BY COUNT(l.id) DESC, f.createdAt DESC
            """)
    List<FreePost> findTopByLikeCount(Pageable pageable);

    @Query("""
            SELECT f
            FROM FreePost f
            JOIN Scrap s ON s.targetId = f.id
            WHERE s.member.id = :memberId
              AND s.targetType = :targetType
              AND (:cursorId IS NULL OR f.id < :cursorId)
            ORDER BY s.createdAt DESC, f.id DESC
            """)
    List<FreePost> findScrappedByMemberIdAndCursor(
            @Param("memberId") Long memberId,
            @Param("targetType") ScrapTargetType targetType,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    void deleteByMemberId(Long memberId);
}
