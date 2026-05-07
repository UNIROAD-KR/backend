package com.uniroad.backend.domain.chat.entity;

import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatReferenceType referenceType;

    private Long referenceId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomMember> members = new ArrayList<>();

    public static ChatRoom create(ChatReferenceType type, Long refId) {
        ChatRoom room = new ChatRoom();
        room.referenceType = type;
        room.referenceId = refId;
        return room;
    }
}
