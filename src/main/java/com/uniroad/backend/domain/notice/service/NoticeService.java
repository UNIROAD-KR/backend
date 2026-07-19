package com.uniroad.backend.domain.notice.service;

import com.uniroad.backend.domain.notice.dto.NoticeRequest;
import com.uniroad.backend.domain.notice.dto.NoticeResponse;
import com.uniroad.backend.domain.notice.entity.Notice;
import com.uniroad.backend.domain.notice.repository.NoticeRepository;
import com.uniroad.backend.domain.notification.service.NotificationService;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NotificationService notificationService;

    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAll().stream()
                .map(NoticeResponse::from)
                .toList();
    }

    public NoticeResponse getNotice(Long noticeId) {
        return NoticeResponse.from(findNotice(noticeId));
    }

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.title().trim())
                .content(request.content().trim())
                .build();
        Notice saved = noticeRepository.save(notice);
        notificationService.notifyNotice(
                saved.getId(),
                saved.getTitle(),
                saved.getContent()
        );
        return NoticeResponse.from(saved);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeRequest request) {
        Notice notice = findNotice(noticeId);
        notice.update(request.title().trim(), request.content().trim());
        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = findNotice(noticeId);
        noticeRepository.delete(notice);
    }

    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
