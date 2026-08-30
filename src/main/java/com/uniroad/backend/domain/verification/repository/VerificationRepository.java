package com.uniroad.backend.domain.verification.repository;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.verification.entity.Verification;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findByMemberAndIsCurrentTrue(Member member);

    List<Verification> findAllByMemberIdOrderBySubmittedAtDesc(Long memberId);

    /** 제출한 본인만 자기 서류 이미지를 조회할 수 있는지 확인할 때 사용한다 */
    boolean existsByMemberIdAndImageUrl(Long memberId, String imageUrl);

    @EntityGraph(attributePaths = {"member"})
    List<Verification> findAllByStatusAndIsCurrentTrue(VerificationStatus status);

    long countByStatusAndIsCurrentTrue(VerificationStatus status);

    void deleteByMemberId(Long memberId);
}
