package com.uniroad.backend.domain.chat.controller;

import com.uniroad.backend.domain.chat.dto.ChatMessageRequest;
import com.uniroad.backend.domain.chat.dto.ChatMessageResponse;
import com.uniroad.backend.domain.chat.dto.ChatRoomRequest;
import com.uniroad.backend.domain.chat.dto.ChatRoomResponse;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomRepository;
import com.uniroad.backend.domain.chat.service.ChatRoomService;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Chat", description = "채팅 관련 API (채팅방 생성, 목록 조회, 메시지 내역 등)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/rooms")
public class ChatRoomController {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성 또는 조회", description = "게시글 정보를 바탕으로 채팅방을 생성하거나, 이미 존재하는 경우 기존 방을 반환합니다.")
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody ChatRoomRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        
        ChatRoom chatRoom = chatRoomService.getOrCreateChatRoomByMemberIds(
                request.getReferenceType(),
                request.getReferenceId(),
                Arrays.asList(memberId, request.getTargetMemberId())
        );
        
        return ResponseEntity.ok(ChatRoomResponse.from(chatRoom));
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "로그인한 사용자가 참여 중인 모든 채팅방 목록을 조회합니다.")
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

    @Operation(summary = "채팅 메시지 내역 조회", description = "특정 채팅방의 이전 메시지 내역을 페이징하여 조회합니다.")
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
