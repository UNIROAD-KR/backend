package com.uniroad.backend.domain.notification.event;

import com.uniroad.backend.domain.notification.service.FcmService;
import com.uniroad.backend.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 푸시는 알림 행이 실제로 커밋된 뒤에만 나가야 한다.
 * 트랜잭션이 롤백됐는데 푸시만 나가면, 사용자는 알림을 눌렀을 때 아무것도 없는 화면을 만난다.
 *
 * 발송 실패가 원래 작업(채팅 저장 등)을 되돌리면 안 되므로 예외를 여기서 삼킨다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationEventListener {

    private final FcmService fcmService;

    @Async(AsyncConfig.PUSH_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PushNotificationEvent event) {
        try {
            fcmService.sendToMemberId(
                    event.memberId(),
                    event.title(),
                    event.body(),
                    event.data(),
                    event.channelId(),
                    event.collapseKey()
            );
        } catch (Exception e) {
            log.warn("푸시 발송에 실패했습니다. memberId={}", event.memberId(), e);
        }
    }

    @Async(AsyncConfig.PUSH_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NoticeBroadcastEvent event) {
        try {
            fcmService.broadcast(
                    event.title(),
                    event.body(),
                    event.data(),
                    event.channelId(),
                    event.collapseKey()
            );
        } catch (Exception e) {
            log.warn("공지 푸시 발송에 실패했습니다.", e);
        }
    }
}
