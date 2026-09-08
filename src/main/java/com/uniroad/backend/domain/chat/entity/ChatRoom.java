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

    // Hibernate는 MySQL에서 enum 상수를 네이티브 ENUM 컬럼으로 만드는데,
    // ddl-auto=update는 이미 만들어진 ENUM의 허용 값을 넓혀주지 않는다.
    // 그대로 두면 참조 타입을 새로 추가할 때마다 INSERT가 데이터 오류로 죽으므로 varchar로 못박는다.
    @Enumerated(EnumType.STRING)
    @Column(length = 30, columnDefinition = "varchar(30)")
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
