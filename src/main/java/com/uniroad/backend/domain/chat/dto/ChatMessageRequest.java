package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    private Long roomId;
    private String message;
    private MessageType type;
}
