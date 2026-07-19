package com.uniroad.backend.domain.notification.service;

import com.uniroad.backend.domain.chat.entity.ChatMessage;
import com.uniroad.backend.domain.chat.entity.ChatRoomMember;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.notification.dto.NotificationResponse;
import com.uniroad.backend.domain.notification.dto.UnreadCountResponse;
import com.uniroad.backend.domain.notification.entity.Notification;
import com.uniroad.backend.domain.notification.entity.NotificationType;
import com.uniroad.backend.domain.notification.repository.NotificationRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatPresenceService chatPresenceService;
    private final FcmService fcmService;

    public Page<NotificationResponse> getUnreadNotifications(Long memberId, Pageable pageable) {
        Member member = getMember(memberId);
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(member, pageable)
                .map(NotificationResponse::from);
    }

    public Page<NotificationResponse> getNotifications(Long memberId, Pageable pageable) {
        Member member = getMember(memberId);
        return notificationRepository.findByUserOrderByCreatedAtDesc(member, pageable)
                .map(NotificationResponse::from);
    }

    public UnreadCountResponse getUnreadCount(Long memberId) {
        Member member = getMember(memberId);
        return new UnreadCountResponse(notificationRepository.countByUserAndReadFalse(member));
    }

    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = getOwnedNotification(memberId, notificationId);
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        Member member = getMember(memberId);
        notificationRepository.updateReadByUserAndReadFalse(member, true);
    }

    @Transactional
    public void delete(Long memberId, Long notificationId) {
        Notification notification = getOwnedNotification(memberId, notificationId);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAll(Long memberId) {
        Member member = getMember(memberId);
        notificationRepository.deleteByUser(member);
    }

    @Transactional
    public void notifyChatMessage(ChatMessage message) {
        Long roomId = message.getChatRoom().getId();

        chatRoomMemberRepository.findByChatRoomAndLeftAtIsNull(message.getChatRoom()).stream()
                .map(ChatRoomMember::getMember)
                .filter(member -> !member.getId().equals(message.getSenderId()))
                .filter(member -> shouldNotifyChat(member.getId(), roomId))
                .forEach(receiver -> createChatNotification(receiver, message));
    }

    @Transactional
    public void notifyNotice(Long noticeId, String title, String content) {
        List<Member> members = memberRepository.findAll();
        members.forEach(member -> createNotification(
                member,
                NotificationType.NOTICE,
                title,
                content,
                noticeId
        ));
    }

    private boolean shouldNotifyChat(Long receiverId, Long roomId) {
        return !chatPresenceService.isAppActive(receiverId)
                || !chatPresenceService.isViewingRoom(receiverId, roomId);
    }

    private void createChatNotification(Member receiver, ChatMessage message) {
        Long roomId = message.getChatRoom().getId();
        String title = "새 채팅 메시지";
        String content = message.getMessage();

        Notification notification = createNotification(
                receiver,
                NotificationType.CHAT,
                title,
                content,
                roomId
        );

        fcmService.sendToMember(receiver, title, content, Map.of(
                "type", NotificationType.CHAT.name(),
                "roomId", String.valueOf(roomId),
                "referenceId", String.valueOf(roomId),
                "notificationId", String.valueOf(notification.getId())
        ));
    }

    private Notification createNotification(Member receiver, NotificationType type, String title, String content, Long referenceId) {
        return notificationRepository.save(Notification.builder()
                .user(receiver)
                .type(type)
                .title(title)
                .content(content)
                .referenceId(referenceId)
                .build());
    }

    private Notification getOwnedNotification(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!notification.getUser().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return notification;
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
