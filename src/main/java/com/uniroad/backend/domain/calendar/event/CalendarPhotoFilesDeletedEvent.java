package com.uniroad.backend.domain.calendar.event;

import java.util.List;

/**
 * "이 S3 객체들은 더 이상 어떤 사진 행도 가리키지 않는다"는 사실만 담는다.
 * 행 삭제가 커밋된 뒤에 처리된다.
 */
public record CalendarPhotoFilesDeletedEvent(List<String> keys) {
}
