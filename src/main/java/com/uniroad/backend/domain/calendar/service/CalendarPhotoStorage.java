package com.uniroad.backend.domain.calendar.service;

import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarPhotoUploadResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 캘린더 사진이 S3 어디에 놓이는지를 아는 유일한 곳.
 *
 * 개인 기록이라 공개 경로(images/)가 아니라 회원별 비공개 경로에 둔다.
 * key에 회원 ID를 넣어 두면, 일정에 사진을 붙일 때 key만 보고 본인이 올린 파일인지 가릴 수 있다.
 */
@Component
@RequiredArgsConstructor
public class CalendarPhotoStorage {

    private static final String PREFIX = "private/calendar/";
    private static final String THUMBNAIL_SUFFIX = "_thumb";
    private static final int MAX_KEY_LENGTH = 500;

    // 그리드 한 화면에 수십 장이 뜨므로 인증 서류(10분)보다 길게 준다.
    // 그동안 앱이 같은 화면을 다시 그려도 URL이 살아 있다.
    private static final Duration READ_URL_EXPIRATION = Duration.ofHours(1);

    // HEIC는 안드로이드가 표시하지 못해 받지 않는다. 앱이 올리기 전에 JPEG로 바꾼다.
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Service s3Service;

    public CalendarPhotoUploadResponse.Item createUpload(Long memberId, CalendarPhotoUploadRequest.Item item) {
        String thumbnailContentType = item.thumbnailContentType() != null
                ? item.thumbnailContentType()
                : item.contentType();

        // 파일 이름은 받지 않는다. 사용자가 준 이름을 key에 넣으면 경로 조작과 개인정보 노출의 여지가 생긴다.
        String base = memberPrefix(memberId) + UUID.randomUUID();
        String key = base + "." + extensionOf(item.contentType());
        String thumbnailKey = base + THUMBNAIL_SUFFIX + "." + extensionOf(thumbnailContentType);

        return new CalendarPhotoUploadResponse.Item(
                key,
                s3Service.createUploadUrl(key, item.contentType()),
                thumbnailKey,
                s3Service.createUploadUrl(thumbnailKey, thumbnailContentType)
        );
    }

    /** 이 회원이 발급받은 업로드 key가 맞는지 확인한다. */
    public void validateOwnedKey(Long memberId, String key) {
        String prefix = memberPrefix(memberId);
        if (key == null
                || key.length() > MAX_KEY_LENGTH
                || !key.startsWith(prefix)
                || key.substring(prefix.length()).contains("/")) {
            throw new CustomException(ErrorCode.INVALID_CALENDAR_PHOTO);
        }
    }

    public String readUrl(String key) {
        return s3Service.createReadUrl(key, READ_URL_EXPIRATION);
    }

    private String memberPrefix(Long memberId) {
        return PREFIX + memberId + "/";
    }

    private String extensionOf(String contentType) {
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new CustomException(ErrorCode.INVALID_CALENDAR_PHOTO);
        }
        return extension;
    }
}
