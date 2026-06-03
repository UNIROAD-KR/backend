package com.uniroad.backend.domain.community.freepost.repository;

import com.uniroad.backend.domain.community.freepost.entity.FreePost;
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
              AND (
                    :keyword IS NULL
                    OR LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY f.id DESC
            """)
    List<FreePost> findByCursorAndKeyword(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
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
            LEFT JOIN FreePostLike l ON l.freePost = f
            GROUP BY f
            ORDER BY COUNT(l.id) DESC, f.createdAt DESC
            """)
    List<FreePost> findTopByLikeCount(Pageable pageable);
}
