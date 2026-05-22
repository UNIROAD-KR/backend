package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.PartnerUniversity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerUniversityRepository extends JpaRepository<PartnerUniversity, Long> {

    @Query(
            value = """
                    SELECT *
                    FROM partner_university pu
                    WHERE (:country IS NULL OR pu.country_id IN (
                        SELECT c.id FROM country c WHERE c.name = :country OR c.code = :country
                      ))
                      AND (:keyword IS NULL OR LOWER(pu.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:major IS NULL OR JSON_SEARCH(pu.supported_majors, 'one', CONCAT('%', :major, '%')) IS NOT NULL)
                      AND (:language IS NULL OR JSON_SEARCH(pu.class_languages, 'one', CONCAT('%', :language, '%')) IS NOT NULL)
                      AND (:dormitoryAvailable IS NULL OR pu.dormitory_available = :dormitoryAvailable)
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM partner_university pu
                    WHERE (:country IS NULL OR pu.country_id IN (
                        SELECT c.id FROM country c WHERE c.name = :country OR c.code = :country
                      ))
                      AND (:keyword IS NULL OR LOWER(pu.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:major IS NULL OR JSON_SEARCH(pu.supported_majors, 'one', CONCAT('%', :major, '%')) IS NOT NULL)
                      AND (:language IS NULL OR JSON_SEARCH(pu.class_languages, 'one', CONCAT('%', :language, '%')) IS NOT NULL)
                      AND (:dormitoryAvailable IS NULL OR pu.dormitory_available = :dormitoryAvailable)
                    """,
            nativeQuery = true
    )
    Page<PartnerUniversity> search(
            @Param("country") String country,
            @Param("keyword") String keyword,
            @Param("major") String major,
            @Param("language") String language,
            @Param("dormitoryAvailable") Boolean dormitoryAvailable,
            Pageable pageable
    );

    long countByCountryId(Long countryId);
}
