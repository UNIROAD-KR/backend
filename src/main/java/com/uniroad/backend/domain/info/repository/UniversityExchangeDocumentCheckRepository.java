package com.uniroad.backend.domain.info.repository;

import com.uniroad.backend.domain.info.entity.UniversityExchangeDocumentCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniversityExchangeDocumentCheckRepository extends JpaRepository<UniversityExchangeDocumentCheck, Long> {
    List<UniversityExchangeDocumentCheck> findByExchangeInfoIdAndMemberId(Long exchangeInfoId, Long memberId);
    Optional<UniversityExchangeDocumentCheck> findByExchangeInfoIdAndMemberIdAndDocumentId(
            Long exchangeInfoId, Long memberId, Long documentId);
    void deleteByMemberId(Long memberId);
}
