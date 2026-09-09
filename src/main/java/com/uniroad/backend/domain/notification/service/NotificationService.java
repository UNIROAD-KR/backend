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
import com.uniroad.backend.domain.notification.event.NoticeBroadcastEvent;
import com.uniroad.backend.domain.notification.event.PushNotificationEvent;
import com.uniroad.backend.domain.notification.repository.NotificationRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final NotificationSettingService notificationSettingService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 알림 컬럼 폭. 원문이 더 길면 잘라서 넣는다.
     * 채팅 메시지는 TEXT라 길이 제한이 없고 공지 제목은 200자까지 허용되는데,
     * 그대로 넣으면 INSERT가 터지면서 알림을 만들던 트랜잭션 전체가 롤백된다.
     * 즉 긴 채팅을 보내면 메시지 자체가 저장되지 않고 사라진다.
     */
    private static final int TITLE_LIMIT = 100;
    private static final int CONTENT_LIMIT = 500;

    /** 공지 알림 행을 한 번에 몰아넣지 않고 끊어 저장하는 단위 */
    private static final int NOTICE_BATCH_SIZE = 500;

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

    /**
     * 내 글에 달린 댓글을 글쓴이에게 알린다. 알림함에 행을 남기고, 커밋 뒤 푸시를 한 번 보낸다.
     *
     * 자기 글에 자기가 단 댓글은 알리지 않는다 — 방금 쓴 본인에게 되돌아가는 알림이라 소음만 된다.
     */
    @Transactional
    public void notifyPostComment(Long postAuthorId, Long postId, String postTitle, Long commenterId, String commentContent) {
        if (postAuthorId.equals(commenterId)) {
            return;
        }

        Member receiver = getMember(postAuthorId);
        String title = "내 글에 댓글이 달렸어요";
        // 알림함에서는 어느 글인지가 먼저 보여야 해서 글 제목을 앞에 붙인다.
        String content = "[" + postTitle + "] " + commentContent;

        Notification notification = createNotification(
                receiver,
                NotificationType.COMMENT,
                title,
                content,
                postId
        );

        // 커뮤니티 알림을 꺼둔 사람에게는 푸시만 보내지 않는다. 알림함 행은 위에서 이미 만들었다.
        if (!notificationSettingService.isPushAllowed(receiver.getId(), NotificationType.COMMENT)) {
            return;
        }

        // 채팅과 같은 이유로 커밋 뒤에 보낸다. 여기서 바로 보내면 댓글 저장 트랜잭션이
        // 구글로 나가는 왕복이 끝날 때까지 열려 있게 된다.
        eventPublisher.publishEvent(new PushNotificationEvent(
                receiver.getId(),
                title,
                content,
                Map.of(
                        "type", NotificationType.COMMENT.name(),
                        "postId", String.valueOf(postId),
                        "referenceId", String.valueOf(postId),
                        "notificationId", String.valueOf(notification.getId())
                ),
                NotificationType.COMMENT.channelId(),
                // 같은 글의 댓글 알림은 하나로 덮어쓴다. 댓글 열 개에 푸시 열 개가 쌓이지 않도록.
                "comment-" + postId
        ));
    }

    /**
     * 교환학생 인증이 승인됐음을 본인에게 알린다.
     *
     * 사용자는 서류를 올린 뒤 승인이 언제 났는지 알 방법이 없어서, 앱을 열어 직접
     * 확인하기 전에는 인증 회원 기능이 열린 줄 모른다. 결과를 기다리게 하는 종류의
     * 알림이라 SYSTEM으로 보낸다 — 종류별로 끌 수 없고 전체 알림을 껐을 때만 막힌다.
     *
     * 권한은 이 알림과 무관하게 이미 올라가 있다. 인증 필터가 요청마다 회원을 다시 읽으므로
     * 다시 로그인하지 않아도 곧바로 인증 회원 기능을 쓸 수 있다.
     */
    @Transactional
    public void notifyVerificationApproved(Member receiver, Long verificationId) {
        String title = "교환학생 인증이 완료되었어요";
        String content = "이제 중고거래와 티켓 양도, 동행 구하기 글을 올릴 수 있어요.";

        Notification notification = createNotification(
                receiver,
                NotificationType.VERIFICATION,
                title,
                content,
                verificationId
        );

        if (!notificationSettingService.isPushAllowed(receiver.getId(), NotificationType.VERIFICATION)) {
            return;
        }

        // 다른 알림과 같은 이유로 커밋 뒤에 보낸다. 승인 트랜잭션이 구글로 나가는
        // 왕복이 끝날 때까지 열려 있으면 안 된다.
        eventPublisher.publishEvent(new PushNotificationEvent(
                receiver.getId(),
                title,
                content,
                Map.of(
                        "type", NotificationType.VERIFICATION.name(),
                        "verificationId", String.valueOf(verificationId),
                        "referenceId", String.valueOf(verificationId),
                        "notificationId", String.valueOf(notification.getId())
                ),
                NotificationType.VERIFICATION.channelId(),
                // 인증 결과는 한 번뿐이라 묶을 이유가 없다. 다시 신청해 또 승인되면
                // 그건 별개의 소식이므로 verificationId로 구분한다.
                "verification-" + verificationId
        ));
    }

    /**
     * 교환학생 인증이 반려됐음을 본인에게 알린다.
     *
     * 승인보다 이쪽이 더 중요하다. 승인은 늦게 알아도 손해가 없지만, 반려는 알려주지 않으면
     * 사용자가 계속 기다리기만 하고 서류를 다시 올릴 생각을 하지 못한다.
     * 그래서 사유를 본문에 실어 무엇을 고쳐야 하는지까지 함께 보낸다.
     */
    @Transactional
    public void notifyVerificationRejected(Member receiver, Long verificationId, String reason) {
        String title = "교환학생 인증이 반려되었어요";
        // 관리자가 사유를 비워 둘 수 있다. 그때 "사유: null"이 뜨지 않게 문구를 나눈다.
        String content = (reason == null || reason.isBlank())
                ? "서류를 다시 확인한 뒤 인증을 다시 신청해주세요."
                : "사유: " + reason.trim();

        Notification notification = createNotification(
                receiver,
                NotificationType.VERIFICATION,
                title,
                content,
                verificationId
        );

        if (!notificationSettingService.isPushAllowed(receiver.getId(), NotificationType.VERIFICATION)) {
            return;
        }

        eventPublisher.publishEvent(new PushNotificationEvent(
                receiver.getId(),
                title,
                content,
                Map.of(
                        "type", NotificationType.VERIFICATION.name(),
                        "verificationId", String.valueOf(verificationId),
                        "referenceId", String.valueOf(verificationId),
                        "notificationId", String.valueOf(notification.getId())
                ),
                NotificationType.VERIFICATION.channelId(),
                "verification-" + verificationId
        ));
    }

    /**
     * 공지를 전 회원에게 알린다. 앱 알림함에 남길 행을 만들고, 커밋 뒤 푸시를 한 번 쏜다.
     *
     * 회원 엔티티를 통째로 올리지 않고 id만 읽어 프록시로 참조를 건다.
     * 저장도 한 번에 몰아넣지 않고 끊어서 flush 한다 — 회원이 늘어도 메모리가 함께 늘지 않게.
     */
    @Transactional
    public void notifyNotice(Long noticeId, String title, String content) {
        List<Long> memberIds = memberRepository.findAllMemberIds();

        String safeTitle = truncate(title, TITLE_LIMIT);
        String safeContent = truncate(content, CONTENT_LIMIT);

        for (int start = 0; start < memberIds.size(); start += NOTICE_BATCH_SIZE) {
            List<Long> chunk = memberIds.subList(
                    start, Math.min(start + NOTICE_BATCH_SIZE, memberIds.size()));

            List<Notification> rows = chunk.stream()
                    .map(memberId -> Notification.builder()
                            // getReferenceById는 프록시만 만든다 — 회원을 다시 조회하지 않는다
                            .user(memberRepository.getReferenceById(memberId))
                            .type(NotificationType.NOTICE)
                            .title(safeTitle)
                            .content(safeContent)
                            .referenceId(noticeId)
                            .build())
                    .toList();

            notificationRepository.saveAll(rows);
            notificationRepository.flush();
        }

        // 내용이 모두 같으므로 회원마다 이벤트를 띄우지 않고 한 번만 띄운다.
        // 발송 쪽이 토큰을 묶어 보낸다.
        eventPublisher.publishEvent(new NoticeBroadcastEvent(
                safeTitle,
                safeContent,
                Map.of(
                        "type", NotificationType.NOTICE.name(),
                        "noticeId", String.valueOf(noticeId),
                        "referenceId", String.valueOf(noticeId)
                ),
                NotificationType.NOTICE.channelId(),
                "notice-" + noticeId
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

        // 알림함 행은 위에서 이미 만들었다. 여기서 막는 것은 푸시뿐이라,
        // 채팅 알림을 꺼둔 사람도 앱에서 알림함을 열면 놓친 메시지를 볼 수 있다.
        if (!notificationSettingService.isPushAllowed(receiver.getId(), NotificationType.CHAT)) {
            return;
        }

        // 발송은 커밋 뒤에 다른 스레드가 맡는다. 여기서 바로 보내면
        // 구글로 나가는 왕복이 끝날 때까지 채팅 저장 트랜잭션이 열려 있게 된다.
        eventPublisher.publishEvent(new PushNotificationEvent(
                receiver.getId(),
                title,
                content,
                Map.of(
                        "type", NotificationType.CHAT.name(),
                        "roomId", String.valueOf(roomId),
                        "referenceId", String.valueOf(roomId),
                        "notificationId", String.valueOf(notification.getId())
                ),
                NotificationType.CHAT.channelId(),
                // 같은 방의 알림은 하나로 덮어쓴다. 안 그러면 메시지 열 개에 알림 열 개가 쌓인다.
                "chat-" + roomId
        ));
    }

    private Notification createNotification(Member receiver, NotificationType type, String title, String content, Long referenceId) {
        return notificationRepository.save(Notification.builder()
                .user(receiver)
                .type(type)
                .title(truncate(title, TITLE_LIMIT))
                .content(truncate(content, CONTENT_LIMIT))
                .referenceId(referenceId)
                .build());
    }

    /** 컬럼 폭을 넘기면 잘라낸다. 잘렸다는 표시로 말줄임표를 붙인다. */
    private String truncate(String value, int limit) {
        if (value == null) {
            return "";
        }
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
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
