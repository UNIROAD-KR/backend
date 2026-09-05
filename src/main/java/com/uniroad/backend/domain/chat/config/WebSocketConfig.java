package com.uniroad.backend.domain.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
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

    /** 하트비트 주기(ms). 양방향 동일하게 둔다. */
    private static final long HEARTBEAT_MILLIS = 15_000L;
    private final StompHandler stompHandler;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * 하트비트를 보내는 주체.
     *
     * @EnableWebSocketMessageBroker가 등록해 주는 messageBrokerTaskScheduler를 그대로 쓴다.
     * 생성자로 받으면 이 설정 클래스와 순환 참조가 생기므로 세터로 늦게 주입받는다.
     */
    private TaskScheduler messageBrokerTaskScheduler;

    @Autowired
    public void setMessageBrokerTaskScheduler(
            @Lazy @Qualifier("messageBrokerTaskScheduler") TaskScheduler taskScheduler
    ) {
        this.messageBrokerTaskScheduler = taskScheduler;
    }

    /**
     * 하트비트를 켜서 죽은 연결을 빨리 알아챈다.
     *
     * 모바일은 지하철·엘리베이터·기내모드로 연결이 소리 없이 끊긴다. 클라이언트가
     * DISCONNECT 프레임을 못 보내면 서버는 TCP가 죽은 걸 한참 뒤에나 아는데,
     * 그동안 ChatPresenceService는 그 사용자가 "방을 보고 있다"고 판단해
     * 새 메시지의 푸시를 계속 억제한다. 즉 알림을 통째로 놓친다.
     *
     * 15초는 배터리와 감지 속도의 절충이다. 하트비트 프레임 자체는 개행 한 글자라
     * 3초 폴링과는 비교가 안 되게 가볍지만, 짧을수록 라디오를 자주 깨운다.
     * 값은 [서버→클라이언트, 클라이언트→서버] 순서이며, 실제 주기는 클라이언트가
     * CONNECT에서 제시한 값과 협상해 정해진다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub")
                .setHeartbeatValue(new long[] {HEARTBEAT_MILLIS, HEARTBEAT_MILLIS})
                .setTaskScheduler(messageBrokerTaskScheduler);
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
