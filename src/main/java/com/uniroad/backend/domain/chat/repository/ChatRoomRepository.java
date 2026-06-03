package com.uniroad.backend.domain.chat.repository;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByReferenceTypeAndReferenceId(ChatReferenceType referenceType, Long referenceId);

    @Query("""
            SELECT crm.chatRoom
            FROM ChatRoomMember crm
            WHERE crm.chatRoom.referenceType = :referenceType
              AND crm.chatRoom.referenceId = :referenceId
              AND crm.member.id IN :memberIds
              AND crm.leftAt IS NULL
            GROUP BY crm.chatRoom
            HAVING COUNT(DISTINCT crm.member.id) = :memberCount
            """)
    Optional<ChatRoom> findActiveRoomByReferenceAndMemberIds(
            @Param("referenceType") ChatReferenceType referenceType,
            @Param("referenceId") Long referenceId,
            @Param("memberIds") List<Long> memberIds,
            @Param("memberCount") long memberCount
    );
}
