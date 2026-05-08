package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatRoomRequest {
    @Schema(description = "참조 타입 (TRADE, MENTOR 등)", example = "TRADE")
    private ChatReferenceType referenceType;

    @Schema(description = "참조 대상 ID (게시글 ID 등)", example = "1")
    private Long referenceId;

    @Schema(description = "채팅 상대방 멤버 ID", example = "2")
    private Long targetMemberId;

    public ChatReferenceType getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public Long getTargetMemberId() {
        return targetMemberId;
    }
}
