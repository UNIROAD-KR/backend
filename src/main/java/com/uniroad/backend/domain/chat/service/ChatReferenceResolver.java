package com.uniroad.backend.domain.chat.service;

import com.uniroad.backend.domain.chat.dto.ChatReferenceSummary;
import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.ticket.repository.TicketTransferRepository;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 채팅방이 가리키는 게시글의 제목과 대표 이미지를 찾는다.
 *
 * 방 목록은 10초마다 폴링되므로 방마다 글을 조회하지 않고, 참조 타입별로 한 번씩만 모아서 조회한다.
 * 글이 지워졌거나 연결된 게시판이 없는 타입(MENTOR)은 결과에서 빠진다.
 */
@Component
@RequiredArgsConstructor
public class ChatReferenceResolver {

    private record Key(ChatReferenceType type, Long id) {
    }

    private final UsedItemRepository usedItemRepository;
    private final TicketTransferRepository ticketTransferRepository;
    private final CompanionPostRepository companionPostRepository;

    /** 방 ID → 참조 글 요약. 찾지 못한 방은 들어 있지 않다. */
    public Map<Long, ChatReferenceSummary> resolve(Collection<ChatRoom> rooms) {
        Map<ChatReferenceType, Set<Long>> idsByType = rooms.stream()
                .filter(room -> room.getReferenceType() != null && room.getReferenceId() != null)
                .collect(Collectors.groupingBy(
                        ChatRoom::getReferenceType,
                        Collectors.mapping(ChatRoom::getReferenceId, Collectors.toSet())
                ));

        Map<Key, ChatReferenceSummary> summaries = new HashMap<>();
        idsByType.forEach((type, ids) -> load(type, ids)
                .forEach((id, summary) -> summaries.put(new Key(type, id), summary)));

        Map<Long, ChatReferenceSummary> byRoomId = new HashMap<>();
        for (ChatRoom room : rooms) {
            ChatReferenceSummary summary = summaries.get(new Key(room.getReferenceType(), room.getReferenceId()));
            if (summary != null) {
                byRoomId.put(room.getId(), summary);
            }
        }
        return byRoomId;
    }

    public ChatReferenceSummary resolve(ChatRoom room) {
        return resolve(List.of(room)).get(room.getId());
    }

    private Map<Long, ChatReferenceSummary> load(ChatReferenceType type, Set<Long> ids) {
        Map<Long, ChatReferenceSummary> result = new HashMap<>();
        switch (type) {
            case TRADE -> usedItemRepository.findChatReferenceSummaries(ids).forEach(row -> {
                String thumbnail = (String) row[2];
                String firstImage = (String) row[3];
                String imageUrl = thumbnail != null && !thumbnail.isBlank() ? thumbnail : firstImage;
                result.put((Long) row[0], new ChatReferenceSummary((String) row[1], imageUrl));
            });
            // 티켓 양도와 동행 글에는 이미지가 없다.
            case TICKET -> ticketTransferRepository.findChatReferenceSummaries(ids)
                    .forEach(row -> result.put((Long) row[0], new ChatReferenceSummary((String) row[1], null)));
            case COMPANION -> companionPostRepository.findChatReferenceSummaries(ids)
                    .forEach(row -> result.put((Long) row[0], new ChatReferenceSummary((String) row[1], null)));
            case MENTOR -> {
                // 연결된 게시판이 없다.
            }
        }
        return result;
    }
}
