package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;

public interface WebSocketNotificationService {

    /**
     * Push event đến user cụ thể qua /user/{userId}/queue/notifications
     */
    void pushToUser(Integer userId, NotificationEvent event);

    /**
     * Broadcast event đến tất cả subscriber của topic
     * Ví dụ: /topic/admin/dashboard
     */
    void pushToTopic(String topic, NotificationEvent event);
}
