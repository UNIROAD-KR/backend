package com.uniroad.backend.domain.chat.config;

import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.service.ChatRoomService;
import com.uniroad.backend.domain.notification.service.ChatPresenceService;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.jwt.JwtProvider;
import com.uniroad.backend.global.security.CustomUserDetailsService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private static final String CHAT_ROOM_DESTINATION_PREFIX = "/sub/chat/room/";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ChatPresenceService chatPresenceService;
    private final ChatRoomService chatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // wrap()은 헤더를 복사한 새 accessor를 돌려주므로 setUser()가 원본 메시지에 반영되지 않는다.
        // CONNECT에서 붙인 인증 정보가 사라져 이후 SUBSCRIBE에서 principal이 null이 된다.
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT == command) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE == command) {
            handleSubscribe(accessor);
        } else if (StompCommand.UNSUBSCRIBE == command) {
            chatPresenceService.unsubscribe(accessor.getSessionId(), accessor.getSubscriptionId());
        } else if (StompCommand.DISCONNECT == command) {
            chatPresenceService.disconnect(accessor.getSessionId());
        }

        return message;
    }

    /**
     * 웹소켓도 HTTP와 같은 기준으로 막는다.
     *
     * 예전에는 서명과 만료만 확인해서, Refresh Token으로도 채팅에 붙을 수 있었고
     * 로그아웃한 뒤의 토큰도 그대로 통했다. 지금은 Access Token만 받고,
     * 연결 시점에 회원을 한 번 읽어 회수된 토큰인지까지 확인한다.
     * (연결당 한 번뿐이라 메시지마다 드는 비용은 없다.)
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        String jwt = accessor.getFirstNativeHeader("Authorization");
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        Long memberId;
        try {
            Claims claims = jwtProvider.parseAccessClaims(jwt);
            memberId = jwtProvider.getMemberId(claims);
            userDetailsService.loadUserById(memberId, jwtProvider.getTokenVersion(claims));
        } catch (CustomException e) {
            log.warn("[Stomp] CONNECT 거부: {}", e.getMessage());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(memberId, null, null);
        accessor.setUser(authentication);
        chatPresenceService.connect(accessor.getSessionId(), memberId);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Long roomId = extractRoomId(accessor.getDestination());
        if (roomId != null) {
            validateRoomSubscription(accessor, roomId);
            chatPresenceService.subscribeRoom(accessor.getSessionId(), accessor.getSubscriptionId(), roomId);
        }
    }

    private void validateRoomSubscription(StompHeaderAccessor accessor, Long roomId) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        ChatRoom chatRoom = chatRoomService.findById(roomId);
        chatRoomService.getActiveRoomMember(chatRoom, Long.parseLong(principal.getName()));
    }

    private Long extractRoomId(String destination) {
        if (destination == null || !destination.startsWith(CHAT_ROOM_DESTINATION_PREFIX)) {
            return null;
        }

        try {
            return Long.parseLong(destination.substring(CHAT_ROOM_DESTINATION_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
