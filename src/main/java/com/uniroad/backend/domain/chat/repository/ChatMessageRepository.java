package com.uniroad.backend.domain.chat.repository;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    /**
     * 여러 방의 마지막 메시지를 한 번에 가져온다.
     * id가 auto increment라 같은 방 안에서는 id가 큰 쪽이 항상 나중 메시지다.
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom IN :rooms "
            + "AND m.id = (SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.chatRoom = m.chatRoom)")
    List<ChatMessage> findLastMessagesByChatRoomIn(@Param("rooms") List<ChatRoom> rooms);
    Optional<ChatMessage> findFirstByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);
    long countByChatRoomAndSenderIdNotAndCreatedAtAfter(ChatRoom chatRoom, Long senderId, LocalDateTime lastReadAt);
    long countByChatRoomAndSenderIdNot(ChatRoom chatRoom, Long senderId);
    void deleteBySenderId(Long senderId);
}
