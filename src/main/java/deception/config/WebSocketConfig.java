package deception.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Địa chỉ để Frontend kết nối WebSocket vào (VD: ws://localhost:8080/ws-game)
        // setAllowedOriginPatterns("*") giúp tránh lỗi CORS khi test ở local
        registry.addEndpoint("/ws-game").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố cho các kênh mà Server sẽ phát sóng (Client sẽ subscribe vào đây)
        registry.enableSimpleBroker("/topic");

        // Tiền tố cho các request mà Client gửi trực tiếp qua WebSocket (Nếu cần)
        registry.setApplicationDestinationPrefixes("/app");
    }
}