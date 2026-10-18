package com.uniroad.backend.domain.chat;

import com.uniroad.backend.domain.chat.dto.ChatMessageResponse;
import com.uniroad.backend.domain.chat.dto.ChatRoomResponse;
import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatReferenceType;
import com.uniroad.backend.domain.chat.entity.MessageType;
import com.uniroad.backend.domain.chat.service.ChatRoomService;
import com.uniroad.backend.domain.chat.service.ChatService;
import com.uniroad.backend.domain.companion.entity.CompanionPost;
import com.uniroad.backend.domain.companion.entity.RecruitmentStatus;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.ticket.entity.TicketTransferPost;
import com.uniroad.backend.domain.ticket.entity.TicketType;
import com.uniroad.backend.domain.ticket.repository.TicketTransferRepository;
import com.uniroad.backend.domain.useditem.entity.TradeCategoryImage;
import com.uniroad.backend.domain.useditem.entity.TradeItemCategory;
import com.uniroad.backend.domain.useditem.entity.UsedItemPost;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatRoomAndImageMessageTest {

    private static final String IMAGE_URL =
            "https://uniroad-image-bucket.s3.ap-northeast-2.amazonaws.com/images/uuid_photo.jpg";

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UsedItemRepository usedItemRepository;
    @Autowired
    private TicketTransferRepository ticketTransferRepository;
    @Autowired
    private CompanionPostRepository companionPostRepository;
    @Autowired
    private EntityManager entityManager;

    private Member me;
    private Member other;

    @BeforeEach
    void setUp() {
        me = member("me");
        other = member("other");
    }

    @Test
    @DisplayName("채팅 목록에 참조 글의 제목과 대표 이미지가 함께 내려온다")
    void roomListCarriesReferenceTitleAndImage() {
        UsedItemPost withThumbnail = usedItem("냄비 팝니다", "https://cdn/thumb.jpg", "https://cdn/first.jpg");
        UsedItemPost withoutThumbnail = usedItem("이불 팝니다", null, "https://cdn/first.jpg");
        TicketTransferPost ticket = ticket("기차표 양도");
        CompanionPost companion = companion("파리 동행");
        UsedItemPost toBeDeleted = usedItem("지워질 글", "https://cdn/x.jpg");

        Long tradeRoom = room(ChatReferenceType.TRADE, withThumbnail.getId());
        Long tradeNoThumbRoom = room(ChatReferenceType.TRADE, withoutThumbnail.getId());
        Long ticketRoom = room(ChatReferenceType.TICKET, ticket.getId());
        Long companionRoom = room(ChatReferenceType.COMPANION, companion.getId());
        Long mentorRoom = room(ChatReferenceType.MENTOR, 999L);
        Long deletedRoom = room(ChatReferenceType.TRADE, toBeDeleted.getId());

        usedItemRepository.delete(toBeDeleted);
        sync();

        Map<Long, ChatRoomResponse> rooms = chatRoomService.getMyRooms(me.getId()).stream()
                .collect(Collectors.toMap(ChatRoomResponse::getRoomId, room -> room));

        assertReference(rooms.get(tradeRoom), "냄비 팝니다", "https://cdn/thumb.jpg");
        assertReference(rooms.get(tradeNoThumbRoom), "이불 팝니다", "https://cdn/first.jpg");
        assertReference(rooms.get(ticketRoom), "기차표 양도", null);
        assertReference(rooms.get(companionRoom), "파리 동행", null);
        assertReference(rooms.get(mentorRoom), null, null);
        assertReference(rooms.get(deletedRoom), null, null);
    }

    @Test
    @DisplayName("방을 만들 때 응답에도 참조 글 정보가 들어간다")
    void createRoomResponseCarriesReference() {
        UsedItemPost post = usedItem("냄비 팝니다", "https://cdn/thumb.jpg");

        ChatRoomResponse response = chatRoomService.getOrCreateChatRoomResponse(
                ChatReferenceType.TRADE, post.getId(), me.getId(), other.getId());

        assertReference(response, "냄비 팝니다", "https://cdn/thumb.jpg");
    }

    @Test
    @DisplayName("사진 메시지는 글 없이 보낼 수 있고, 목록에는 '사진을 보냈습니다'로 보인다")
    void imageMessage() {
        Long roomId = room(ChatReferenceType.MENTOR, 1L);

        ChatMessage saved = chatService.saveMessage(roomId, me.getId(), null, MessageType.IMAGE, IMAGE_URL);
        sync();

        assertThat(saved.getImageUrl()).isEqualTo(IMAGE_URL);
        assertThat(saved.getMessage()).isNull();

        List<ChatMessageResponse> messages = chatRoomService.getMessages(roomId, other.getId(), PageRequest.of(0, 20));
        assertThat(messages).singleElement().satisfies(message -> {
            assertThat(message.getType()).isEqualTo(MessageType.IMAGE);
            assertThat(message.getImageUrl()).isEqualTo(IMAGE_URL);
        });

        ChatRoomResponse room = chatRoomService.getMyRooms(other.getId()).get(0);
        assertThat(room.getLastMessage()).isEqualTo(ChatMessage.IMAGE_PREVIEW_TEXT);
        assertThat(room.getLastMessageType()).isEqualTo(MessageType.IMAGE);
    }

    @Test
    @DisplayName("사진 메시지에 우리 버킷 주소가 없으면 거절하고, 일반 메시지의 사진 주소는 저장하지 않는다")
    void imageMessageValidation() {
        Long roomId = room(ChatReferenceType.MENTOR, 1L);

        assertInvalid(() -> chatService.saveMessage(roomId, me.getId(), "사진", MessageType.IMAGE, null));
        assertInvalid(() -> chatService.saveMessage(roomId, me.getId(), null, MessageType.IMAGE, "https://evil.example/images/a.jpg"));
        assertInvalid(() -> chatService.saveMessage(roomId, me.getId(), null, MessageType.IMAGE,
                "https://uniroad-image-bucket.s3.ap-northeast-2.amazonaws.com/private/a.jpg"));
        assertInvalid(() -> chatService.saveMessage(roomId, me.getId(), " ", MessageType.TALK, IMAGE_URL));

        ChatMessage talk = chatService.saveMessage(roomId, me.getId(), " 안녕 ", MessageType.TALK, IMAGE_URL);
        assertThat(talk.getMessage()).isEqualTo("안녕");
        assertThat(talk.getImageUrl()).isNull();
    }

    private void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getCode())
                .isEqualTo(ErrorCode.INVALID_CHAT_MESSAGE.name());
    }

    private void assertReference(ChatRoomResponse room, String title, String imageUrl) {
        assertThat(room.getReferenceTitle()).isEqualTo(title);
        assertThat(room.getReferenceImageUrl()).isEqualTo(imageUrl);
    }

    private Long room(ChatReferenceType type, Long referenceId) {
        return chatRoomService.getOrCreateChatRoomByMemberIds(type, referenceId, List.of(me.getId(), other.getId())).getId();
    }

    private UsedItemPost usedItem(String title, String thumbnail, String... images) {
        UsedItemPost post = UsedItemPost.builder()
                .title(title)
                .content("내용")
                .price(1000L)
                .region("파리")
                .semester("2026-2")
                .author(other)
                .thumbnailImageUrl(thumbnail)
                .build();
        for (String image : images) {
            post.addImage(TradeCategoryImage.builder().imageUrl(image).category(TradeItemCategory.ETC).build());
        }
        return usedItemRepository.save(post);
    }

    private TicketTransferPost ticket(String title) {
        return ticketTransferRepository.save(TicketTransferPost.builder()
                .author(other)
                .ticketType(TicketType.TRAIN)
                .title(title)
                .quantity(1)
                .transferPrice(1000L)
                .build());
    }

    private CompanionPost companion(String title) {
        return companionPostRepository.save(CompanionPost.builder()
                .member(other)
                .title(title)
                .content("내용")
                .startDate(LocalDate.parse("2026-11-01"))
                .endDate(LocalDate.parse("2026-11-03"))
                .country("프랑스")
                .region("파리")
                .status(RecruitmentStatus.RECRUITING)
                .capacity(4)
                .currentParticipants(1)
                .build());
    }

    private void sync() {
        entityManager.flush();
        entityManager.clear();
    }

    private Member member(String key) {
        return memberRepository.save(Member.builder()
                .email(key + "@chat.test")
                .password("password")
                .name(key)
                .role(Role.VERIFIED)
                .provider("LOCAL")
                .build());
    }
}
