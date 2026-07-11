package com.uniroad.backend.domain.notification.repository;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByMember(Member member);
    Optional<FcmToken> findByToken(String token);
    void deleteByMemberId(Long memberId);
}
