package com.uniroad.backend.domain.notification.repository;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(Member user, Pageable pageable);
    long countByUserAndReadFalse(Member user);

    @Modifying
    @Query("UPDATE Notification n SET n.read = :read WHERE n.user = :user AND n.read = false")
    int updateReadByUserAndReadFalse(@Param("user") Member user, @Param("read") boolean read);

    @Modifying
    void deleteByUser(Member user);

    void deleteByUserId(Long userId);
}
