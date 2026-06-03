package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long roomId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String message;

    @NotNull(message = "메시지 타입은 필수입니다.")
    private MessageType type;
}
