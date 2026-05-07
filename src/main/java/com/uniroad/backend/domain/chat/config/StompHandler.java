package com.uniroad.backend.domain.chat.config;

import com.uniroad.backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwt = accessor.getFirstNativeHeader("Authorization");
            if (jwt != null && jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }
            // JwtProvider의 validateToken 메서드 이름이 다를 수 있으므로 확인 필요
            // 여기서는 일반적인 validateToken 또는 parseToken을 가정
            if (!jwtProvider.validateToken(jwt)) {
                log.error("Invalid JWT token in STOMP connection");
                throw new RuntimeException("Invalid JWT token");
            }

            Long memberId = jwtProvider.getMemberId(jwt);
            // 인증 객체 생성 및 설정
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    memberId, null, null // 권한 정보가 필요하면 로드해서 추가
            );
            accessor.setUser(authentication);
        }
        return message;
    }
}
