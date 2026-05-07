package com.uniroad.backend.domain.chat.repository;

import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByMember(Member member);
    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
}
