package com.uniroad.backend.domain.notification.service;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.notification.dto.NotificationSettingRequest;
import com.uniroad.backend.domain.notification.dto.NotificationSettingResponse;
import com.uniroad.backend.domain.notification.entity.NotificationCategory;
import com.uniroad.backend.domain.notification.entity.NotificationSetting;
import com.uniroad.backend.domain.notification.entity.NotificationType;
import com.uniroad.backend.domain.notification.repository.NotificationSettingRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원별 알림 on/off를 읽고 쓴다.
 *
 * 설정은 푸시를 보낼지만 정한다. 끈 알림도 알림함에는 그대로 쌓여서,
 * 앱에서 알림함을 열면 놓친 내용을 볼 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;

    public NotificationSettingResponse get(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .map(NotificationSettingResponse::from)
                // 한 번도 저장하지 않았으면 행을 만들지 않고 기본값만 돌려준다.
                // 조회만으로 행이 생기면 회원 수만큼 쓸모없는 기본값 행이 쌓인다.
                .orElseGet(NotificationSettingResponse::defaults);
    }

    @Transactional
    public NotificationSettingResponse update(Long memberId, NotificationSettingRequest request) {
        NotificationSetting setting = getOrCreate(memberId);

        setting.update(
                request.allEnabled(),
                request.chat(),
                request.market(),
                request.community(),
                request.schedule(),
                request.marketing(),
                request.notice()
        );

        return NotificationSettingResponse.from(setting);
    }

    /** 전체 알림 스위치. 끄면 종류별 설정과 관계없이 어떤 푸시도 나가지 않는다. */
    @Transactional
    public NotificationSettingResponse updateAllEnabled(Long memberId, boolean enabled) {
        NotificationSetting setting = getOrCreate(memberId);
        setting.updateAllEnabled(enabled);
        return NotificationSettingResponse.from(setting);
    }

    /** 종류별 스위치 하나. 끈 종류도 알림함에는 그대로 쌓이고 푸시만 나가지 않는다. */
    @Transactional
    public NotificationSettingResponse updateCategory(Long memberId, NotificationCategory category, boolean enabled) {
        NotificationSetting setting = getOrCreate(memberId);
        setting.updateCategory(category, enabled);
        return NotificationSettingResponse.from(setting);
    }

    private NotificationSetting getOrCreate(Long memberId) {
        return notificationSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.createDefault(getMember(memberId))));
    }

    /**
     * 이 회원에게 이 종류의 푸시를 보내도 되는지.
     *
     * 설정 행이 없으면 기본값을 따른다 — 앱을 깔고 한 번도 설정 화면에 들어가지 않은
     * 회원이 대부분이므로, 이 경로가 정상이지 예외가 아니다.
     */
    public boolean isPushAllowed(Long memberId, NotificationType type) {
        return notificationSettingRepository.findByMemberId(memberId)
                .map(setting -> setting.allowsPush(type))
                .orElseGet(() -> isDefaultAllowed(type));
    }

    private boolean isDefaultAllowed(NotificationType type) {
        NotificationCategory category = type.category();
        return category == null || category.isDefaultEnabled();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
