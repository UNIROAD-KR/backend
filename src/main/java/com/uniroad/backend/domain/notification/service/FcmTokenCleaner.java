package com.uniroad.backend.domain.notification.service;

import com.uniroad.backend.domain.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * 죽은 토큰을 지우는 일만 맡는 별도 빈.
 *
 * FcmService 안에 두면 자기 자신을 호출하는 꼴이라 @Transactional이 걸리지 않는다.
 * (프록시를 거치지 않기 때문) 삭제는 반드시 쓰기 트랜잭션이 필요해 빈을 나눴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmTokenCleaner {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 토큰 정리가 실패해도 원래 작업에 영향을 주면 안 되므로 항상 새 트랜잭션에서 돈다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDeadTokens(Collection<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        try {
            fcmTokenRepository.deleteAllByTokenIn(tokens);
            log.info("만료된 FCM 토큰 {}건을 정리했습니다.", tokens.size());
        } catch (Exception e) {
            log.warn("FCM 토큰 정리에 실패했습니다. count={}", tokens.size(), e);
        }
    }
}
