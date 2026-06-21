package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.MessageType;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
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
        validateMessage(content, type);

        ChatRoom chatRoom = chatRoomService.findById(roomId);
        chatRoomService.getActiveRoomMember(chatRoom, senderId).updateLastReadAt();

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .message(content.trim())
                .type(type)
                .build();
        return chatMessageRepository.save(message);
    }

    private void validateMessage(String content, MessageType type) {
        if (type == null || content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CHAT_MESSAGE);
        }
    }
}
