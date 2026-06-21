package com.uniroad.backend.domain.chat.config;

import com.uniroad.backend.domain.chat.entity.ChatRoom;
import com.uniroad.backend.domain.chat.service.ChatRoomService;
import com.uniroad.backend.domain.notification.service.ChatPresenceService;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
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

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private static final String CHAT_ROOM_DESTINATION_PREFIX = "/sub/chat/room/";

    private final JwtProvider jwtProvider;
    private final ChatPresenceService chatPresenceService;
    private final ChatRoomService chatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
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

    private void handleConnect(StompHeaderAccessor accessor) {
        String jwt = accessor.getFirstNativeHeader("Authorization");
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        if (!jwtProvider.validateToken(jwt)) {
            log.error("Invalid JWT token in STOMP connection");
            throw new RuntimeException("Invalid JWT token");
        }

        Long memberId = jwtProvider.getMemberId(jwt);
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
