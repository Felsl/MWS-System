package org.lvtn.mws.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.infrastructure.security.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * [GIAI ĐOẠN 7] Cấu hình WebSocket/STOMP cho thông báo real-time.
 *
 * - Endpoint handshake: /ws (SockJS fallback).
 * - Broker in-memory: /topic (broadcast), /queue (điểm-điểm qua /user).
 * - Đích cá nhân: /user/{userId}/queue/notifications.
 *
 * XÁC THỰC: ChannelInterceptor đọc header "Authorization: Bearer <jwt>" ở khung CONNECT,
 * validate bằng JwtTokenProvider, rồi set Principal = userId để convertAndSendToUser định tuyến đúng.
 *
 * GHI CHÚ SẢN XUẤT: đây là bản cơ bản. Nên siết thêm: chỉ cho phép origin tin cậy (thay "*"),
 * kiểm tra hạn token định kỳ, và cân nhắc xác thực lại ở khung SUBSCRIBE để chặn nghe lén
 * destination của người khác.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider tokenProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        if (tokenProvider.validateToken(token)) {
                            String userId = tokenProvider.extractUserId(token);
                            accessor.setUser((Principal) () -> userId);
                        }
                    }
                }
                return message;
            }
        });
    }
}
