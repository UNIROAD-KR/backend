package com.uniroad.backend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.notification.dto.FcmPushResponse;
import com.uniroad.backend.domain.notification.entity.FcmToken;
import com.uniroad.backend.domain.notification.repository.FcmTokenRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmService {
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Transactional
    public void registerToken(Long memberId, String token) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        fcmToken -> fcmToken.updateMember(member),
                        () -> fcmTokenRepository.save(FcmToken.create(member, token))
                );
    }

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
        List<FcmToken> tokens = fcmTokenRepository.findByMember(member);
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            log.debug("FirebaseMessaging bean is not available. Skip push notification.");
            return new FcmPushResponse(member.getId(), tokens.size(), 0, false);
        }

        int successCount = 0;
        for (FcmToken token : tokens) {
            if (send(firebaseMessaging, token.getToken(), title, body, data)) {
                successCount++;
            }
        }
        return new FcmPushResponse(member.getId(), tokens.size(), successCount, true);
    }

    private boolean send(FirebaseMessaging firebaseMessaging, String token, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();
        try {
            firebaseMessaging.send(message);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send FCM notification. token={}", token, e);
            return false;
        }
    }
}
