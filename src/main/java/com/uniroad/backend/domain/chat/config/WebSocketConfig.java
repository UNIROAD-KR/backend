package com.uniroad.backend.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompHandler stompHandler;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    /**
     * 네이티브 WebSocket과 SockJS 폴백을 모두 등록한다.
     *
     * SockJS만 등록하면 /ws-stomp 로의 순수 WebSocket 핸드셰이크가 실패해서,
     * 브라우저 기본 WebSocket을 쓰는 STOMP 클라이언트가 붙지 못한다.
     * 허용 Origin은 HTTP와 같은 cors.allowed-origins 값을 쓴다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = allowedOrigins.toArray(new String[0]);

        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(origins);

        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(origins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
