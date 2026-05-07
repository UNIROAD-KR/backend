package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.MessageType;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String content, MessageType type) {
        ChatRoom chatRoom = chatRoomService.findById(roomId);
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .message(content)
                .type(type)
                .build();
        return chatMessageRepository.save(message);
    }
}
