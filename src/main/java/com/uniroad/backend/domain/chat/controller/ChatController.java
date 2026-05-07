package com.uniroad.backend.domain.chat.controller;

import com.uniroad.backend.domain.chat.dto.ChatMessageRequest;
import com.uniroad.backend.domain.chat.dto.ChatMessageResponse;
import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageRequest request, java.security.Principal principal) {
        Long senderId = Long.parseLong(principal.getName());
        
        ChatMessage message = chatService.saveMessage(
                request.getRoomId(),
                senderId,
                request.getMessage(),
                request.getType()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + request.getRoomId(), ChatMessageResponse.from(message));
    }
}
