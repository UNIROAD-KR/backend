package com.uniroad.backend.domain.scrap.repository;

import com.uniroad.backend.domain.scrap.entity.Scrap;
import com.uniroad.backend.domain.scrap.entity.ScrapTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    long countByTargetTypeAndTargetId(ScrapTargetType targetType, Long targetId);
    boolean existsByTargetTypeAndTargetIdAndMemberId(ScrapTargetType targetType, Long targetId, Long memberId);
    Optional<Scrap> findByTargetTypeAndTargetIdAndMemberId(ScrapTargetType targetType, Long targetId, Long memberId);
    List<Scrap> findAllByMemberIdAndTargetTypeOrderByCreatedAtDesc(Long memberId, ScrapTargetType targetType);
    void deleteByMemberId(Long memberId);
    void deleteAllByTargetTypeAndTargetId(ScrapTargetType targetType, Long targetId);
}
