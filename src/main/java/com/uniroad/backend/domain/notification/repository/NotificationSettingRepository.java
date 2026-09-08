package com.uniroad.backend.domain.notification.repository;

import com.uniroad.backend.domain.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    /** 푸시 여부 판단은 비동기 발송 직전에도 하므로 엔티티가 아니라 id로 찾는다 */
    Optional<NotificationSetting> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
