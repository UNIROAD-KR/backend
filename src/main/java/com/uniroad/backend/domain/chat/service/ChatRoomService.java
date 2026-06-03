package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.dto.ChatMessageResponse;
import com.uniroad.backend.domain.chat.dto.ChatReadResponse;
import com.uniroad.backend.domain.chat.dto.ChatRoomResponse;
import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoom getOrCreateChatRoom(ChatReferenceType type, Long referenceId, List<Member> members) {
        validateChatMembers(members);

        List<Long> memberIds = members.stream()
                .map(Member::getId)
                .distinct()
                .toList();

        return chatRoomRepository.findActiveRoomByReferenceAndMemberIds(type, referenceId, memberIds, memberIds.size())
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

    public List<ChatRoomResponse> getMyRooms(Long memberId) {
        Member member = getMember(memberId);

        return chatRoomMemberRepository.findByMemberAndLeftAtIsNull(member)
                .stream()
                .map(chatRoomMember -> toRoomResponse(chatRoomMember.getChatRoom(), memberId, chatRoomMember))
                .sorted(Comparator.comparing(
                        response -> response.getLastMessageCreatedAt() != null
                                ? response.getLastMessageCreatedAt()
                                : LocalDateTime.MIN,
                        Comparator.reverseOrder()
                ))
                .toList();
    }

    @Transactional
    public List<ChatMessageResponse> getMessages(Long roomId, Long memberId, Pageable pageable) {
        ChatRoom chatRoom = findById(roomId);
        ChatRoomMember chatRoomMember = getActiveRoomMember(chatRoom, memberId);
        chatRoomMember.updateLastReadAt();

        return chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable)
                .stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatReadResponse markAsRead(Long roomId, Long memberId) {
        ChatRoom chatRoom = findById(roomId);
        ChatRoomMember chatRoomMember = getActiveRoomMember(chatRoom, memberId);
        chatRoomMember.updateLastReadAt();
        return ChatReadResponse.of(roomId, chatRoomMember.getLastReadAt());
    }

    @Transactional
    public void leaveRoom(Long roomId, Long memberId) {
        ChatRoom chatRoom = findById(roomId);
        ChatRoomMember chatRoomMember = getActiveRoomMember(chatRoom, memberId);
        chatRoomMember.leave();
    }

    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    public ChatRoomMember getActiveRoomMember(ChatRoom chatRoom, Long memberId) {
        Member member = getMember(memberId);
        return chatRoomMemberRepository.findByChatRoomAndMemberAndLeftAtIsNull(chatRoom, member)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_ACCESS_DENIED));
    }

    private ChatRoomResponse toRoomResponse(ChatRoom chatRoom, Long currentMemberId, ChatRoomMember currentRoomMember) {
        Member opponent = chatRoomMemberRepository.findByChatRoomAndLeftAtIsNull(chatRoom)
                .stream()
                .map(ChatRoomMember::getMember)
                .filter(member -> !member.getId().equals(currentMemberId))
                .findFirst()
                .orElse(null);

        ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);

        long unreadCount = currentRoomMember.getLastReadAt() == null
                ? chatMessageRepository.countByChatRoomAndSenderIdNot(chatRoom, currentMemberId)
                : chatMessageRepository.countByChatRoomAndSenderIdNotAndCreatedAtAfter(
                        chatRoom,
                        currentMemberId,
                        currentRoomMember.getLastReadAt()
                );

        return ChatRoomResponse.of(
                chatRoom,
                opponent,
                lastMessage != null ? lastMessage.getMessage() : null,
                lastMessage != null ? lastMessage.getType() : null,
                lastMessage != null ? lastMessage.getCreatedAt() : null,
                unreadCount,
                currentRoomMember.getLastReadAt()
        );
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateChatMembers(List<Member> members) {
        long distinctMemberCount = members.stream()
                .map(Member::getId)
                .distinct()
                .count();

        if (members.size() != distinctMemberCount) {
            throw new CustomException(ErrorCode.CANNOT_CHAT_WITH_SELF);
        }
    }
}
