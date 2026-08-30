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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public ChatRoomResponse getOrCreateChatRoomResponse(ChatReferenceType type, Long referenceId, Long memberId, Long targetMemberId) {
        ChatRoom chatRoom = getOrCreateChatRoomByMemberIds(type, referenceId, List.of(memberId, targetMemberId));
        ChatRoomMember currentRoomMember = getActiveRoomMember(chatRoom, memberId);
        return toRoomResponse(chatRoom, memberId, currentRoomMember);
    }

    /**
     * 방 목록은 10초마다 폴링되므로 방 개수에 비례해 쿼리가 늘지 않도록 한 번에 모아 조회한다.
     * 예전에는 방마다 상대방·마지막 메시지·안 읽은 수를 따로 조회해서 방이 늘수록 쿼리가 선형으로 증가했다.
     */
    public List<ChatRoomResponse> getMyRooms(Long memberId) {
        Member member = getMember(memberId);

        List<ChatRoomMember> myMemberships = chatRoomMemberRepository.findActiveWithRoomByMember(member);
        if (myMemberships.isEmpty()) {
            return List.of();
        }

        List<ChatRoom> rooms = myMemberships.stream()
                .map(ChatRoomMember::getChatRoom)
                .toList();

        Map<Long, Member> opponentByRoomId = chatRoomMemberRepository.findActiveWithMemberByChatRoomIn(rooms)
                .stream()
                .filter(roomMember -> !roomMember.getMember().getId().equals(memberId))
                .collect(Collectors.toMap(
                        roomMember -> roomMember.getChatRoom().getId(),
                        ChatRoomMember::getMember,
                        (first, second) -> first
                ));

        Map<Long, ChatMessage> lastMessageByRoomId = chatMessageRepository.findLastMessagesByChatRoomIn(rooms)
                .stream()
                .collect(Collectors.toMap(
                        message -> message.getChatRoom().getId(),
                        message -> message,
                        (first, second) -> first
                ));

        Map<Long, Long> unreadByRoomId = new HashMap<>();
        for (Object[] row : chatRoomMemberRepository.countUnreadPerRoom(memberId)) {
            unreadByRoomId.put((Long) row[0], (Long) row[1]);
        }

        return myMemberships.stream()
                .map(chatRoomMember -> {
                    ChatRoom room = chatRoomMember.getChatRoom();
                    ChatMessage lastMessage = lastMessageByRoomId.get(room.getId());
                    return ChatRoomResponse.of(
                            room,
                            opponentByRoomId.get(room.getId()),
                            lastMessage != null ? lastMessage.getMessage() : null,
                            lastMessage != null ? lastMessage.getType() : null,
                            lastMessage != null ? lastMessage.getCreatedAt() : null,
                            unreadByRoomId.getOrDefault(room.getId(), 0L),
                            chatRoomMember.getLastReadAt()
                    );
                })
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

        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomOrderByCreatedAtDesc(chatRoom, pageable)
                .getContent();

        // 프론트가 이 API를 3초마다 폴링하므로 무조건 갱신하면 조회 한 번마다 UPDATE가 나간다.
        // 실제로 읽지 않은 상대방 메시지가 있을 때만 읽음 시각을 옮긴다.
        if (hasUnreadFromOthers(messages, memberId, chatRoomMember.getLastReadAt())) {
            chatRoomMember.updateLastReadAt();
        }

        LocalDateTime myLastReadAt = chatRoomMember.getLastReadAt();
        LocalDateTime opponentLastReadAt = findOpponentLastReadAt(chatRoom, memberId);

        return messages.stream()
                .map(message -> ChatMessageResponse.from(message, memberId, myLastReadAt, opponentLastReadAt))
                .collect(Collectors.toList());
    }

    /**
     * 상대가 어디까지 읽었는지. 내가 보낸 메시지의 "읽음" 표시 기준이 된다.
     * 아직 읽지 않은 참여자가 한 명이라도 있으면 읽지 않은 것으로 본다.
     */
    private LocalDateTime findOpponentLastReadAt(ChatRoom chatRoom, Long memberId) {
        List<ChatRoomMember> opponents = chatRoomMemberRepository.findByChatRoomAndLeftAtIsNull(chatRoom)
                .stream()
                .filter(roomMember -> !roomMember.getMember().getId().equals(memberId))
                .toList();

        if (opponents.isEmpty() || opponents.stream().anyMatch(opponent -> opponent.getLastReadAt() == null)) {
            return null;
        }

        return opponents.stream()
                .map(ChatRoomMember::getLastReadAt)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private boolean hasUnreadFromOthers(List<ChatMessage> messages, Long memberId, LocalDateTime lastReadAt) {
        return messages.stream()
                .anyMatch(message -> !message.getSenderId().equals(memberId)
                        && (lastReadAt == null || message.getCreatedAt().isAfter(lastReadAt)));
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
