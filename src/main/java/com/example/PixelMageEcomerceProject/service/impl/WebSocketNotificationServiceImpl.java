package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import com.pusher.rest.Pusher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {

    private final Pusher pusher;

    @Override
    public void pushToUser(Integer userId, NotificationEvent event) {
        try {
            // Using Option A: User-specific public channel for simplicity without auth endpoint
            String channel = "user." + userId;
            pusher.trigger(channel, event.getType(), event);
            log.info("[Soketi] Pushed {} event to channel={}", event.getType(), channel);
        } catch (Exception e) {
            log.warn("[Soketi] Failed to push {} to user {}: {}", event.getType(), userId, e.getMessage());
        }
    }

    @Override
    public void pushToTopic(String topic, NotificationEvent event) {
        try {
            // Topic channel: e.g. "admin.notifications"
            // Ensure the topic doesn't start with / internally if we appended it before
            String channel = topic.startsWith("/") ? topic.substring(1).replace("/", ".") : topic;
            pusher.trigger(channel, event.getType(), event);
            log.info("[Soketi] Broadcast {} to channel={}", event.getType(), channel);
        } catch (Exception e) {
            log.warn("[Soketi] Failed to broadcast {} to channel {}: {}", event.getType(), topic, e.getMessage());
        }
    }
}
