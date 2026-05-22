package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByName(String name);

    Optional<Country> findByCode(String code);
}
