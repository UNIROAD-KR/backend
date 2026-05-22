package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.UniversityExchangeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityExchangeInfoRepository extends JpaRepository<UniversityExchangeInfo, Long> {

    Optional<UniversityExchangeInfo> findByUniversityId(Long universityId);
}
