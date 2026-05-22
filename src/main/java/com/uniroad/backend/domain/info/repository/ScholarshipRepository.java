package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.Scholarship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {

    @Query("""
            SELECT s
            FROM Scholarship s
            LEFT JOIN s.country c
            WHERE (:country IS NULL OR c.name = :country OR c.code = :country)
              AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.provider) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Scholarship> search(
            @Param("country") String country,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
