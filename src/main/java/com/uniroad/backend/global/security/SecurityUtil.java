package com.uniroad.backend.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;

/**
 * SecurityContext에서 현재 인증된 사용자의 정보를 꺼내기 위한 유틸 클래스
 */
@Slf4j
public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * 현재 인증된 회원의 ID(memberId) 조회
     */
    public static Long getCurrentMemberId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getMemberId();
        }

        try {
            // OAuth2User 등 다른 타입일 경우 fallback 처리 (필요 시)
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
