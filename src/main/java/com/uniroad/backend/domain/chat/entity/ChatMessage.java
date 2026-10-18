package com.uniroad.backend.domain.chat.entity;

import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {
    /** 사진만 보낸 메시지를 채팅 목록과 알림에 보여줄 때 쓰는 문구 */
    public static final String IMAGE_PREVIEW_TEXT = "사진을 보냈습니다";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String message;

    // 다른 enum 컬럼과 같은 이유로 varchar로 못박는다. IMAGE를 더하면서 기존 ENUM 컬럼은 V5가 넓힌다.
    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "varchar(20)")
    private MessageType type;

    /** IMAGE 메시지의 사진 주소. 그 밖의 메시지는 null이다. */
    @Column(length = 1000)
    private String imageUrl;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Long senderId, String message, MessageType type, String imageUrl) {
        this.chatRoom = chatRoom;
        this.senderId = senderId;
        this.message = message;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    /** 채팅 목록의 마지막 메시지와 알림 본문에 쓰는 한 줄 요약. 사진은 글자가 없으므로 고정 문구로 바꾼다. */
    public String getPreviewText() {
        if (type == MessageType.IMAGE && (message == null || message.isBlank())) {
            return IMAGE_PREVIEW_TEXT;
        }
        return message;
    }
}
