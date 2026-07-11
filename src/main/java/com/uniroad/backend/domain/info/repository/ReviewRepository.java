package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
            SELECT r
            FROM Review r
            WHERE (:cursorId IS NULL OR r.id < :cursorId)
              AND (:country IS NULL OR LOWER(r.partnerUniversity.country) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (:type IS NULL OR LOWER(r.type) LIKE LOWER(CONCAT('%', :type, '%')))
              AND (:keyword IS NULL
                    OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY r.id DESC
            """)
    List<Review> search(
            @Param("cursorId") Long cursorId,
            @Param("country") String country,
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    void deleteByMemberId(Long memberId);
}
