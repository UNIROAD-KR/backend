package com.uniroad.backend.domain.chat.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime lastReadAt;

    public static ChatRoomMember create(ChatRoom chatRoom, Member member) {
        ChatRoomMember chatRoomMember = new ChatRoomMember();
        chatRoomMember.chatRoom = chatRoom;
        chatRoomMember.member = member;
        chatRoomMember.lastReadAt = LocalDateTime.now();
        return chatRoomMember;
    }

    public void updateLastReadAt() {
        this.lastReadAt = LocalDateTime.now();
    }
}
