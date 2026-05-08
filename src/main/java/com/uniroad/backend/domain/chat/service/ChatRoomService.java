package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoom getOrCreateChatRoom(ChatReferenceType type, Long referenceId, List<Member> members) {
        return chatRoomRepository.findByReferenceTypeAndReferenceId(type, referenceId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.create(type, referenceId);
                    chatRoomRepository.save(newRoom);
                    for (Member member : members) {
                        ChatRoomMember chatRoomMember = ChatRoomMember.create(newRoom, member);
                        chatRoomMemberRepository.save(chatRoomMember);
                    }
                    return newRoom;
                });
    }

    @Transactional
    public ChatRoom getOrCreateChatRoomByMemberIds(ChatReferenceType type, Long referenceId, List<Long> memberIds) {
        List<Member> members = memberIds.stream()
                .map(id -> memberRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)))
                .collect(Collectors.toList());
        return getOrCreateChatRoom(type, referenceId, members);
    }

    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }
}
