package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.FavoritePartnerUniversity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoritePartnerUniversityRepository extends JpaRepository<FavoritePartnerUniversity, Long> {
    boolean existsByPartnerUniversityIdAndMemberId(Long partnerUniversityId, Long memberId);
    Optional<FavoritePartnerUniversity> findByPartnerUniversityIdAndMemberId(Long partnerUniversityId, Long memberId);
    void deleteByMemberId(Long memberId);
}
