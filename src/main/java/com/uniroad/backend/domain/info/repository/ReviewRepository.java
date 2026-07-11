package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
            SELECT r
            FROM Review r
            WHERE (:partnerUniversityId IS NULL OR r.partnerUniversity.id = :partnerUniversityId)
              AND (:country IS NULL OR LOWER(r.partnerUniversity.country.name) LIKE LOWER(CONCAT('%', :country, '%')))
              AND (:type IS NULL OR LOWER(r.type) LIKE LOWER(CONCAT('%', :type, '%')))
              AND (:keyword IS NULL
                    OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY r.id DESC
            """)
    Page<Review> search(
            @Param("partnerUniversityId") Long partnerUniversityId,
            @Param("country") String country,
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.partnerUniversity.country.id = :countryId
            """)
    long countByCountryId(@Param("countryId") Long countryId);

    void deleteByMemberId(Long memberId);
}
