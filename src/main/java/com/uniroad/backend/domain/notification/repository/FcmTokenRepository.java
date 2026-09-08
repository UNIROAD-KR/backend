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

    /**
     * 공지 푸시 대상 토큰.
     *
     * 전체 알림이나 공지 알림을 끈 회원은 뺀다. 앱은 전체 알림을 끌 때 그 기기의 토큰을
     * 지우지만 다른 기기에 남은 토큰까지 지우지는 못하므로, 발송하는 쪽에서도 한 번 더 거른다.
     *
     * 공지를 꺼도 알림함 행은 그대로 만든다 — 여기서 빼는 것은 푸시 대상뿐이다.
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT t.token FROM FcmToken t
            WHERE t.member.id NOT IN (
                SELECT s.member.id FROM NotificationSetting s
                WHERE s.allEnabled = false OR s.noticeEnabled = false
            )
            """)
    List<String> findTokenValuesForNoticePush();
}
