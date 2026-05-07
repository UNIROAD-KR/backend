package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public ChatRoom getOrCreateChatRoom(ChatReferenceType type, Long referenceId, List<Member> members) {
        ChatRoom chatRoom = chatRoomRepository.findByReferenceTypeAndReferenceId(type, referenceId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.create(type, referenceId);
                    chatRoomRepository.save(newRoom);
                    for (Member member : members) {
                        ChatRoomMember chatRoomMember = ChatRoomMember.create(newRoom, member);
                        chatRoomMemberRepository.save(chatRoomMember);
                    }
                    return newRoom;
                });
        return chatRoom;
    }

    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND)); // 기존 ErrorCode와 CustomException 사용
    }
}
