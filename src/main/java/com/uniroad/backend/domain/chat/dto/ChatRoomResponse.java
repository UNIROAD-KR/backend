package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponse {
    private Long roomId;
    private ChatReferenceType referenceType;
    private Long referenceId;

    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .referenceType(room.getReferenceType())
                .referenceId(room.getReferenceId())
                .build();
    }
}
