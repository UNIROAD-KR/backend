package com.uniroad.backend.domain.chat.controller;

import com.uniroad.backend.domain.chat.dto.ChatMessageResponse;
import com.uniroad.backend.domain.chat.dto.ChatRoomResponse;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/rooms")
public class ChatRoomController {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<ChatRoomResponse> rooms = chatRoomMemberRepository.findByMember(member)
                .stream()
                .map(ChatRoomMember::getChatRoom)
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        List<ChatMessageResponse> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable)
                .stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }
}
