package com.uniroad.backend.domain.chat.controller;

import com.uniroad.backend.domain.chat.dto.ChatMessageRequest;
import com.uniroad.backend.domain.chat.dto.ChatMessageResponse;
import com.uniroad.backend.domain.chat.dto.ChatMessageSendRequest;
import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.MessageType;
import com.uniroad.backend.domain.chat.service.ChatService;
import com.uniroad.backend.domain.notification.service.NotificationService;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;
    private final NotificationService notificationService;

    @MessageMapping("/chat/message")
    public void message(@Valid ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long senderId = Long.parseLong(principal.getName());
        sendMessage(request.getRoomId(), request.getMessage(), request.getType(), senderId);
    }

    @PostMapping("/api/v1/chat/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageSendRequest request
    ) {
        Long senderId = SecurityUtil.getCurrentMemberId();
        ChatMessageResponse response = sendMessage(roomId, request.getMessage(), request.getType(), senderId);
        return ResponseEntity.ok(response);
    }

    private ChatMessageResponse sendMessage(Long roomId, String content, MessageType type, Long senderId) {
        ChatMessage message = chatService.saveMessage(
                roomId,
                senderId,
                content,
                type
        );

        ChatMessageResponse response = ChatMessageResponse.from(message);
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, response);
        notificationService.notifyChatMessage(message);
        return response;
    }
}
