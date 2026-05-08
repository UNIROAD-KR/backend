package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅방 응답 정보")
public class ChatRoomResponse {
    @Schema(description = "채팅방 ID")
    private Long roomId;

    @Schema(description = "참조 타입")
    private ChatReferenceType referenceType;

    @Schema(description = "참조 대상 ID")
    private Long referenceId;

    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .referenceType(room.getReferenceType())
                .referenceId(room.getReferenceId())
                .build();
    }
}
