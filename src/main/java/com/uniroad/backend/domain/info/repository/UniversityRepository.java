package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {

    Optional<University> findByName(String name);
}
