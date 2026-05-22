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
            JOIN r.partnerUniversity pu
            JOIN pu.country c
            WHERE (:partnerUniversityId IS NULL OR pu.id = :partnerUniversityId)
              AND (:country IS NULL OR c.name = :country OR c.code = :country)
            """)
    Page<Review> search(
            @Param("partnerUniversityId") Long partnerUniversityId,
            @Param("country") String country,
            Pageable pageable
    );
}
