package com.example.PixelMageEcomerceProject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
                // Allow FE (Next.js), mobile dev servers, and Swagger
                .setAllowedOriginPatterns("*")
                // SockJS fallback for browsers that don't support native WebSocket
                .withSockJS();
    }
}
