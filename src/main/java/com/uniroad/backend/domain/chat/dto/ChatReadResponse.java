package com.uniroad.backend.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅방 읽음 처리 응답")
public class ChatReadResponse {
    @Schema(description = "채팅방 ID")
    private Long roomId;

    @Schema(description = "마지막 읽음 일시")
    private LocalDateTime lastReadAt;

    public static ChatReadResponse of(Long roomId, LocalDateTime lastReadAt) {
        return ChatReadResponse.builder()
                .roomId(roomId)
                .lastReadAt(lastReadAt)
                .build();
    }
}
