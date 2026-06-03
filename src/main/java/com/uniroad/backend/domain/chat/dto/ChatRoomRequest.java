package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatRoomRequest {
    @NotNull(message = "참조 타입은 필수입니다.")
    @Schema(description = "참조 타입 (TRADE, MENTOR 등)", example = "TRADE")
    private ChatReferenceType referenceType;

    @NotNull(message = "참조 대상 ID는 필수입니다.")
    @Schema(description = "참조 대상 ID (게시글 ID 등)", example = "1")
    private Long referenceId;

    @NotNull(message = "채팅 상대방 멤버 ID는 필수입니다.")
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
