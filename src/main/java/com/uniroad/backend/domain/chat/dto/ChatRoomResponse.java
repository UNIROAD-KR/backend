package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.MessageType;
import com.uniroad.backend.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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

    @Schema(description = "상대방 멤버 ID")
    private Long opponentMemberId;

    @Schema(description = "상대방 이름")
    private String opponentName;

    @Schema(description = "상대방 닉네임")
    private String opponentNickname;

    @Schema(description = "마지막 메시지")
    private String lastMessage;

    @Schema(description = "마지막 메시지 타입")
    private MessageType lastMessageType;

    @Schema(description = "마지막 메시지 생성 일시")
    private LocalDateTime lastMessageCreatedAt;

    @Schema(description = "읽지 않은 메시지 수")
    private long unreadCount;

    @Schema(description = "마지막 읽음 일시")
    private LocalDateTime lastReadAt;

    public static ChatRoomResponse of(
            ChatRoom room,
            Member opponent,
            String lastMessage,
            MessageType lastMessageType,
            LocalDateTime lastMessageCreatedAt,
            long unreadCount,
            LocalDateTime lastReadAt
    ) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .referenceType(room.getReferenceType())
                .referenceId(room.getReferenceId())
                .opponentMemberId(opponent != null ? opponent.getId() : null)
                .opponentName(opponent != null ? opponent.getName() : null)
                .opponentNickname(opponent != null ? opponent.getNickname() : null)
                .lastMessage(lastMessage)
                .lastMessageType(lastMessageType)
                .lastMessageCreatedAt(lastMessageCreatedAt)
                .unreadCount(unreadCount)
                .lastReadAt(lastReadAt)
                .build();
    }

    public static ChatRoomResponse from(ChatRoom room) {
        return of(room, null, null, null, null, 0, null);
    }
}
