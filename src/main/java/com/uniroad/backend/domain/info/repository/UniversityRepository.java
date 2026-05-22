package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long> {
}
