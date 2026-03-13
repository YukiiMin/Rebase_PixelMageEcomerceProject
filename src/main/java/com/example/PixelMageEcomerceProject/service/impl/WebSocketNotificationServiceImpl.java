package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void pushToUser(Integer userId, NotificationEvent event) {
        try {
            // Destinations: /user/{userId}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    event
            );
            log.info("[WS] Pushed {} event to userId={}", event.getType(), userId);
        } catch (Exception e) {
            // Non-fatal: WebSocket push không nên làm fail business logic chính
            log.warn("[WS] Failed to push {} to userId={}: {}", event.getType(), userId, e.getMessage());
        }
    }

    @Override
    public void pushToTopic(String topic, NotificationEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/" + topic, event);
            log.info("[WS] Broadcast {} to /topic/{}", event.getType(), topic);
        } catch (Exception e) {
            log.warn("[WS] Failed to broadcast {} to /topic/{}: {}", event.getType(), topic, e.getMessage());
        }
    }
}
