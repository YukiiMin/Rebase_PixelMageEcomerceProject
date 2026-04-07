package com.example.PixelMageEcomerceProject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    @org.springframework.beans.factory.annotation.Value("${app.dev-frontend.url:http://localhost:3000}")
    private String devFrontendUrl;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-memory broker: /topic (broadcast), /queue (user-specific)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for @MessageMapping endpoints
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific destinations: /user/{userId}/queue/notifications
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Allow explicit origins for production and development
                .setAllowedOriginPatterns(
                        frontendUrl,
                        devFrontendUrl,
                        "http://localhost:3000",
                        "https://*.railway.app",
                        "https://*.up.railway.app",
                        "*")
                // SockJS fallback for browsers that don't support native WebSocket
                .withSockJS();
    }
}
