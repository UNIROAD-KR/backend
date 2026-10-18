package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
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

    @Schema(description = "참조 게시글 제목 (글이 지워졌거나 연결된 게시판이 없으면 null)")
    private String referenceTitle;

    @Schema(description = "참조 게시글 대표 이미지 주소 (이미지가 없는 게시판이거나 글이 지워졌으면 null)")
    private String referenceImageUrl;

    @Schema(description = "상대방 멤버 ID")
    private Long opponentMemberId;

    @Schema(description = "상대방 이름")
    private String opponentName;

    @Schema(description = "상대방 닉네임")
    private String opponentNickname;

    @Schema(description = "마지막 메시지. 사진만 보낸 메시지는 \"사진을 보냈습니다\"")
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
            ChatReferenceSummary reference,
            Member opponent,
            ChatMessage lastMessage,
            long unreadCount,
            LocalDateTime lastReadAt
    ) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .referenceType(room.getReferenceType())
                .referenceId(room.getReferenceId())
                .referenceTitle(reference != null ? reference.title() : null)
                .referenceImageUrl(reference != null ? reference.imageUrl() : null)
                .opponentMemberId(opponent != null ? opponent.getId() : null)
                .opponentName(opponent != null ? opponent.getName() : null)
                .opponentNickname(opponent != null ? opponent.getNickname() : null)
                .lastMessage(lastMessage != null ? lastMessage.getPreviewText() : null)
                .lastMessageType(lastMessage != null ? lastMessage.getType() : null)
                .lastMessageCreatedAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unreadCount(unreadCount)
                .lastReadAt(lastReadAt)
                .build();
    }

    public static ChatRoomResponse from(ChatRoom room) {
        return of(room, null, null, null, 0, null);
    }
}
