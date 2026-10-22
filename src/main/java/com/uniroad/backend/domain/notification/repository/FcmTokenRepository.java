package com.uniroad.backend.domain.notification.repository;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByMember(Member member);

    /** 푸시 발송은 트랜잭션 밖(비동기)에서 도므로 엔티티가 아니라 id로 찾는다 */
    List<FcmToken> findByMemberId(Long memberId);

    Optional<FcmToken> findByToken(String token);

    void deleteByMemberId(Long memberId);

    void deleteByToken(String token);

    void deleteAllByTokenIn(Collection<String> tokens);

    /** 공지처럼 전체에게 보낼 때 — 엔티티 대신 토큰 문자열만 읽는다 */
    @org.springframework.data.jpa.repository.Query("SELECT t.token FROM FcmToken t")
    List<String> findAllTokenValues();
}
