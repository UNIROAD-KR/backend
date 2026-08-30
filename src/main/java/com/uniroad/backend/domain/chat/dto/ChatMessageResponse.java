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

    @Schema(description = "현재 로그인 사용자의 기준 읽음 여부")
    private boolean isRead;

    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .type(message.getType())
                .createdAt(message.getCreatedAt())
                .isRead(false)
                .build();
    }

    /**
     * 내가 보낸 메시지는 "상대가 읽었는지", 받은 메시지는 "내가 읽었는지"를 나타낸다.
     *
     * 예전에는 내 메시지를 무조건 읽음으로 표시해서, 상대가 보지 않았는데도
     * 화면에 항상 "읽음"이 뜨고 있었다.
     */
    public static ChatMessageResponse from(ChatMessage message, Long currentMemberId,
                                           LocalDateTime myLastReadAt, LocalDateTime opponentLastReadAt) {
        LocalDateTime readerLastReadAt = message.getSenderId().equals(currentMemberId)
                ? opponentLastReadAt
                : myLastReadAt;
        boolean read = readerLastReadAt != null && !message.getCreatedAt().isAfter(readerLastReadAt);

        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .type(message.getType())
                .createdAt(message.getCreatedAt())
                .isRead(read)
                .build();
    }
}
