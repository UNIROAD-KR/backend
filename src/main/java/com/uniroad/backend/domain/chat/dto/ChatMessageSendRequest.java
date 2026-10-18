package com.uniroad.backend.domain.chat.dto;

import com.uniroad.backend.domain.chat.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 정보 (HTTP)")
public class ChatMessageSendRequest {
    @Schema(description = "메시지 내용. TALK는 필수, IMAGE는 선택(사진과 함께 보낼 글)", example = "안녕하세요!")
    private String message;

    @NotNull(message = "메시지 타입은 필수입니다.")
    @Schema(description = "메시지 타입 (TALK, IMAGE 등)", example = "TALK")
    private MessageType type;

    @Schema(description = "사진 주소. IMAGE일 때 필수이며 /api/s3/presigned-url로 올리고 받은 fileUrl을 넣는다.",
            example = "https://uniroad-image-bucket.s3.ap-northeast-2.amazonaws.com/images/uuid_photo.jpg")
    private String imageUrl;
}
