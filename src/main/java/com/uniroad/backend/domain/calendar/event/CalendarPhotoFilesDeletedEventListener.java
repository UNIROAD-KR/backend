package com.uniroad.backend.domain.calendar.event;

import com.uniroad.backend.global.config.AsyncConfig;
import com.uniroad.backend.global.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 사진 파일은 행 삭제가 커밋된 뒤에만 지운다.
 * 트랜잭션이 롤백됐는데 파일만 사라지면 사진 행이 없는 파일을 가리키게 되고, 되돌릴 방법이 없다.
 *
 * S3 호출은 블로킹 HTTP라 푸시 발송과 같은 이유로 요청 스레드에서 떼어낸다(AsyncConfig 참고).
 * 삭제에 실패해도 사용자의 요청은 이미 끝난 뒤이므로 로그만 남긴다. 남은 파일은 어떤 행도 가리키지 않아
 * 화면에 드러나지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarPhotoFilesDeletedEventListener {

    private final S3Service s3Service;

    @Async(AsyncConfig.PUSH_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CalendarPhotoFilesDeletedEvent event) {
        try {
            s3Service.deleteObjects(event.keys());
        } catch (Exception e) {
            log.warn("캘린더 사진 파일 삭제에 실패했습니다. keys={}", event.keys(), e);
        }
    }
}
