package com.uniroad.backend.domain.notification.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.notification.dto.FcmPushResponse;
import com.uniroad.backend.domain.notification.entity.FcmToken;
import com.uniroad.backend.domain.notification.repository.FcmTokenRepository;
import com.uniroad.backend.domain.notification.repository.NotificationRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmService {

    /** sendEachForMulticast 한 번에 실을 수 있는 토큰 수 상한 */
    private static final int MULTICAST_LIMIT = 500;

    /** 푸시 본문이 길면 어차피 잘려 보이고 페이로드(4KB)만 잡아먹는다 */
    private static final int PUSH_BODY_LIMIT = 200;

    /** 채널을 지정하지 않은 푸시가 떨어질 자리. 앱도 같은 이름으로 채널을 만들어야 한다. */
    private static final String DEFAULT_CHANNEL_ID = "default";

    /** apns-collapse-id는 64바이트를 넘을 수 없다 */
    private static final int COLLAPSE_ID_LIMIT = 64;

    /** Expo 푸시 토큰은 FCM으로 보낼 수 없다 */
    private static final String EXPO_TOKEN_PREFIX = "ExponentPushToken";

    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final FcmTokenCleaner fcmTokenCleaner;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    // ── 토큰 관리 ────────────────────────────────────────────

    @Transactional
    public void registerToken(Long memberId, String token) {
        // Expo 토큰은 Expo 서버로 보내야 하는 것이라 FCM에서는 INVALID_ARGUMENT로 전부 실패한다.
        // 게다가 아래 죽은 토큰 정리가 그걸 지워버려서, 로그를 보지 않으면
        // "등록은 되는데 알림이 안 온다"로만 보인다. 받는 자리에서 바로 막는다.
        if (token != null && token.startsWith(EXPO_TOKEN_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        // 같은 기기에 다른 계정이 로그인한 경우다. 주인을 바꿔야
                        // 이전 사용자의 알림이 이 기기로 계속 가지 않는다.
                        fcmToken -> fcmToken.updateMember(member),
                        () -> fcmTokenRepository.save(FcmToken.create(member, token))
                );
    }

    /**
     * 로그아웃한 기기로 푸시가 계속 가지 않도록 지운다.
     *
     * 토큰을 실제로 들고 있는 쪽만 지울 수 있어야 하므로 소유자를 확인한다.
     * 이미 없거나 남의 토큰이면 조용히 넘어간다 — 로그아웃을 실패시킬 이유가 없다.
     */
    @Transactional
    public void deleteToken(Long memberId, String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        fcmTokenRepository.findByToken(token)
                .filter(fcmToken -> fcmToken.getMember().getId().equals(memberId))
                .ifPresent(fcmTokenRepository::delete);
    }

    // ── 발송 ────────────────────────────────────────────────

    public FcmPushResponse sendTestToMember(Long memberId, String title, String body, Map<String, String> data) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Map<String, String> payload = new HashMap<>();
        if (data != null) {
            payload.putAll(data);
        }
        payload.putIfAbsent("type", "TEST");
        payload.put("targetMemberId", String.valueOf(memberId));

        return sendToMember(member, title, body, payload);
    }

    public FcmPushResponse sendToMember(Member member, String title, String body, Map<String, String> data) {
        return send(member.getId(), fcmTokenRepository.findByMember(member), title, body, data, null, null);
    }

    /** 비동기 리스너용 — 트랜잭션 밖에서 도므로 엔티티 대신 id로 토큰을 읽는다 */
    public FcmPushResponse sendToMemberId(
            Long memberId,
            String title,
            String body,
            Map<String, String> data,
            String channelId,
            String collapseKey
    ) {
        return send(memberId, fcmTokenRepository.findByMemberId(memberId), title, body, data, channelId, collapseKey);
    }

    /**
     * 공지처럼 전 회원에게 같은 내용을 보낸다.
     *
     * 회원 단위로 발송하면 회원 수만큼 조회와 호출이 생긴다. 내용이 같으므로
     * 토큰만 모아 500개씩 묶어 보낸다 — 기기 1만 대면 호출 20번이면 끝난다.
     */
    public int broadcast(String title, String body, Map<String, String> data, String channelId, String collapseKey) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.debug("FirebaseMessaging 빈이 없어 공지 푸시를 건너뜁니다.");
            return 0;
        }

        List<String> tokens = fcmTokenRepository.findTokenValuesForNoticePush();
        if (tokens.isEmpty()) {
            return 0;
        }

        Map<String, String> payload = data == null ? Map.of() : data;
        int successCount = 0;
        List<String> deadTokens = new ArrayList<>();

        for (int start = 0; start < tokens.size(); start += MULTICAST_LIMIT) {
            List<String> chunk = tokens.subList(start, Math.min(start + MULTICAST_LIMIT, tokens.size()));
            successCount += sendChunk(
                    firebaseMessaging, chunk, title, body, payload, channelId, collapseKey, null, deadTokens);
        }

        fcmTokenCleaner.deleteDeadTokens(deadTokens);
        log.info("공지 푸시 발송: 대상 {}대 / 성공 {}대", tokens.size(), successCount);
        return successCount;
    }

    private FcmPushResponse send(
            Long memberId,
            List<FcmToken> tokens,
            String title,
            String body,
            Map<String, String> data,
            String channelId,
            String collapseKey
    ) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.debug("FirebaseMessaging 빈이 없어 푸시를 건너뜁니다. memberId={}", memberId);
            return new FcmPushResponse(memberId, tokens.size(), 0, false);
        }
        if (tokens.isEmpty()) {
            return new FcmPushResponse(memberId, 0, 0, true);
        }

        List<String> tokenValues = tokens.stream().map(FcmToken::getToken).toList();
        Map<String, String> payload = data == null ? Map.of() : data;

        // iOS 앱 아이콘에 붙는 숫자. 안 읽은 알림 수를 그대로 쓴다.
        Integer badge = (int) notificationRepository.countByUserIdAndReadFalse(memberId);

        int successCount = 0;
        List<String> deadTokens = new ArrayList<>();

        // 한 사람의 기기가 500대를 넘을 일은 없지만, FCM의 상한이라 그대로 지킨다
        for (int start = 0; start < tokenValues.size(); start += MULTICAST_LIMIT) {
            List<String> chunk = tokenValues.subList(
                    start, Math.min(start + MULTICAST_LIMIT, tokenValues.size()));
            successCount += sendChunk(
                    firebaseMessaging, chunk, title, body, payload, channelId, collapseKey, badge, deadTokens);
        }

        // 앱 삭제·토큰 교체로 영구히 죽은 토큰은 지운다.
        // 두면 계속 쌓이면서 발송 때마다 헛된 왕복을 만든다.
        fcmTokenCleaner.deleteDeadTokens(deadTokens);

        return new FcmPushResponse(memberId, tokenValues.size(), successCount, true);
    }

    private int sendChunk(
            FirebaseMessaging firebaseMessaging,
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data,
            String channelId,
            String collapseKey,
            Integer badge,
            List<String> deadTokens
    ) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(truncate(body, PUSH_BODY_LIMIT))
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .putAllData(data)
                .setAndroidConfig(androidConfig(channelId, collapseKey))
                .setApnsConfig(apnsConfig(collapseKey, badge))
                .build();

        try {
            BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(message);
            List<SendResponse> responses = batchResponse.getResponses();

            for (int i = 0; i < responses.size(); i++) {
                SendResponse response = responses.get(i);
                if (response.isSuccessful()) {
                    continue;
                }

                FirebaseMessagingException exception = response.getException();
                if (isPermanentlyDead(exception)) {
                    deadTokens.add(tokens.get(i));
                } else {
                    // 일시적 오류(네트워크·서버 장애)는 토큰 잘못이 아니므로 지우지 않는다
                    log.warn("FCM 발송 실패(일시적일 수 있음). errorCode={}",
                            exception == null ? "UNKNOWN" : exception.getMessagingErrorCode());
                }
            }

            return batchResponse.getSuccessCount();
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 일괄 발송에 실패했습니다. errorCode={}", e.getMessagingErrorCode(), e);
            return 0;
        }
    }

    /**
     * UNREGISTERED는 앱이 지워졌거나 토큰이 교체된 것이고,
     * INVALID_ARGUMENT는 토큰 형식 자체가 잘못된 것이다. 둘 다 다시 시도해도 살아나지 않는다.
     */
    private boolean isPermanentlyDead(FirebaseMessagingException exception) {
        if (exception == null) {
            return false;
        }
        MessagingErrorCode code = exception.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT;
    }

    /**
     * Android는 채널이 소리·헤드업 표시를 결정한다. 지정하지 않으면 SDK 기본 채널로 떨어져
     * 앱이 만든 채널 설정이 무시된다.
     *
     * 묶음은 AndroidConfig.collapseKey가 아니라 알림 tag로 한다.
     * collapseKey는 기기당 4개까지만 유지돼, 방이 많으면 FCM이 임의로 버린다.
     */
    private AndroidConfig androidConfig(String channelId, String collapseKey) {
        AndroidNotification.Builder notification = AndroidNotification.builder()
                .setChannelId(channelId == null || channelId.isBlank() ? DEFAULT_CHANNEL_ID : channelId)
                // 채널이 없는 구형(8.0 미만) 기기를 위한 값
                .setSound("default")
                .setPriority(AndroidNotification.Priority.HIGH);

        if (collapseKey != null && !collapseKey.isBlank()) {
            notification.setTag(collapseKey);
        }

        // notification 메시지는 이미 high가 기본값이지만, 나중에 data 전용으로 바꿔도
        // 조용히 normal로 떨어지지 않도록 명시해 둔다.
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(notification.build())
                .build();
    }

    /**
     * iOS는 sound를 넣지 않으면 무음으로 뜬다. 화면이 꺼져 있으면 사용자가 알 방법이 없다.
     * 뱃지는 안 읽은 알림 수를 그대로 올린다.
     */
    private ApnsConfig apnsConfig(String collapseKey, Integer badge) {
        Aps.Builder aps = Aps.builder().setSound("default");

        // 공지처럼 여러 사람에게 같은 메시지를 보낼 때는 사람마다 숫자가 달라 넣을 수 없다.
        // 틀린 숫자를 올리느니 건드리지 않는다.
        if (badge != null) {
            aps.setBadge(badge);
        }

        ApnsConfig.Builder config = ApnsConfig.builder()
                // 10 = 즉시 전달
                .putHeader("apns-priority", "10")
                .setAps(aps.build());

        if (collapseKey != null && !collapseKey.isBlank() && collapseKey.length() <= COLLAPSE_ID_LIMIT) {
            config.putHeader("apns-collapse-id", collapseKey);
        }

        return config.build();
    }

    private String truncate(String value, int limit) {
        if (value == null) {
            return "";
        }
        return value.length() <= limit ? value : value.substring(0, limit) + "…";
    }
}
