package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅 메시지 응답 정보")
public class ChatMessageResponse {
    @Schema(description = "메시지 ID")
    private Long id;

    @Schema(description = "채팅방 ID")
    private Long roomId;

    @Schema(description = "발신자 ID")
    private Long senderId;

    @Schema(description = "메시지 내용")
    private String message;

    @Schema(description = "메시지 타입 (TALK, ENTER 등)")
    private MessageType type;

    @Schema(description = "메시지 생성 일시")
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .type(message.getType())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
