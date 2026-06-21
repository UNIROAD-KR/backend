package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 정보 (HTTP)")
public class ChatMessageSendRequest {
    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String message;

    @NotNull(message = "메시지 타입은 필수입니다.")
    @Schema(description = "메시지 타입 (TALK, ENTER 등)", example = "TALK")
    private MessageType type;
}
