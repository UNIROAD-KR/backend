package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.MessageType;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.domain.notification.service.NotificationService;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final S3Service s3Service;

    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String content, MessageType type, String imageUrl) {
        validateMessage(content, type, imageUrl);

        ChatRoom chatRoom = chatRoomService.findById(roomId);
        chatRoomService.getActiveRoomMember(chatRoom, senderId).updateLastReadAt();

        String trimmed = content == null || content.isBlank() ? null : content.trim();
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .message(trimmed)
                .type(type)
                .imageUrl(type == MessageType.IMAGE ? imageUrl : null)
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(message);
        notificationService.notifyChatMessage(savedMessage);
        return savedMessage;
    }

    /**
     * 사진 메시지는 우리 버킷에 올린 사진 주소가 있어야 하고 글은 없어도 된다.
     * 그 밖의 메시지는 예전처럼 글이 있어야 한다.
     */
    private void validateMessage(String content, MessageType type, String imageUrl) {
        if (type == null) {
            throw new CustomException(ErrorCode.INVALID_CHAT_MESSAGE);
        }
        if (type == MessageType.IMAGE) {
            if (!s3Service.isPublicImageUrl(imageUrl)) {
                throw new CustomException(ErrorCode.INVALID_CHAT_MESSAGE);
            }
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CHAT_MESSAGE);
        }
    }
}
