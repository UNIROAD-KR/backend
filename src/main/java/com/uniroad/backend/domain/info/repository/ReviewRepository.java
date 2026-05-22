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
            WHERE (:partnerUniversityId IS NULL OR pu.id = :partnerUniversityId)
              AND (:countryCode IS NULL OR pu.countryCode = :countryCode)
            """)
    Page<Review> search(
            @Param("partnerUniversityId") Long partnerUniversityId,
            @Param("countryCode") String countryCode,
            Pageable pageable
    );
}
