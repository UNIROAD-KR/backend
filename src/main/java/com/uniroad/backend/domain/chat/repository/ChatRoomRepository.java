package com.uniroad.backend.domain.chat.repository;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByReferenceTypeAndReferenceId(ChatReferenceType referenceType, Long referenceId);
}
