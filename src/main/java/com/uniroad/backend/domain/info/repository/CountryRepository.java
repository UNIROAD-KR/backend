package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByName(String name);

    Optional<Country> findByCode(String code);

    @Query("""
            SELECT c
            FROM Country c
            LEFT JOIN PartnerUniversity pu ON pu.country = c
            LEFT JOIN Review r ON r.partnerUniversity = pu
            GROUP BY c
            ORDER BY COUNT(r.id) DESC, COUNT(DISTINCT pu.id) DESC, c.name ASC
            """)
    List<Country> findPopularCountries();
}
