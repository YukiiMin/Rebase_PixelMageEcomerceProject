package com.example.PixelMageEcomerceProject.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    /**
     * Event types:
     * NFC_LINKED, NFC_UNLINKED, PAYMENT_CONFIRMED, ORDER_STATUS_CHANGED
     */
    private String type;

    private Integer userId;

    /** Flexible payload: depends on event type */
    private Map<String, Object> payload;

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();

    // ── Static factory methods ──────────────────────────────────────────

    public static NotificationEvent nfcLinked(Integer userId, Map<String, Object> cardInfo) {
        return NotificationEvent.builder()
                .type("NFC_LINKED")
                .userId(userId)
                .payload(cardInfo)
                .build();
    }

    public static NotificationEvent nfcUnlinked(Integer userId, Map<String, Object> cardInfo) {
        return NotificationEvent.builder()
                .type("NFC_UNLINKED")
                .userId(userId)
                .payload(cardInfo)
                .build();
    }

    public static NotificationEvent paymentConfirmed(Integer userId, Integer orderId, String paymentIntentId) {
        return NotificationEvent.builder()
                .type("PAYMENT_CONFIRMED")
                .userId(userId)
                .payload(Map.of(
                        "orderId", orderId,
                        "paymentIntentId", paymentIntentId != null ? paymentIntentId : ""
                ))
                .build();
    }

    public static NotificationEvent orderStatusChanged(Integer userId, Integer orderId, String newStatus) {
        return NotificationEvent.builder()
                .type("ORDER_STATUS_CHANGED")
                .userId(userId)
                .payload(Map.of(
                        "orderId", orderId,
                        "status", newStatus
                ))
                .build();
    }

    // ── Admin Dashboard real-time events ────────────────────────────────────

    /**
     * Broadcast khi User mới đăng ký thành công.
     * Topic: /topic/admin.notifications
     */
    public static NotificationEvent newUserRegistered(Integer userId, String name, String email) {
        return NotificationEvent.builder()
                .type("NEW_USER_REGISTERED")
                .userId(userId)
                .payload(Map.of(
                        "userId", userId,
                        "name",   name != null ? name : "",
                        "email",  email != null ? email : ""
                ))
                .build();
    }

    /**
     * Broadcast khi User bắt đầu một phiên Tarot.
     * Topic: /topic/admin.notifications
     * mode: "EXPLORE" | "YOUR_DECK"
     */
    public static NotificationEvent tarotSessionStarted(Integer userId, Integer sessionId, String mode, String spreadName) {
        return NotificationEvent.builder()
                .type("TAROT_SESSION_STARTED")
                .userId(userId)
                .payload(Map.of(
                        "sessionId",  sessionId,
                        "mode",       mode != null ? mode : "EXPLORE",
                        "spreadName", spreadName != null ? spreadName : ""
                ))
                .build();
    }

    /**
     * Broadcast khi phiên Tarot hoàn thành (AI trả lời xong).
     * Topic: /topic/admin.notifications
     */
    public static NotificationEvent tarotSessionCompleted(Integer userId, Integer sessionId, String mode) {
        return NotificationEvent.builder()
                .type("TAROT_SESSION_COMPLETED")
                .userId(userId)
                .payload(Map.of(
                        "sessionId", sessionId,
                        "mode",      mode != null ? mode : "EXPLORE"
                ))
                .build();
    }

    /**
     * Broadcast khi đơn hàng được thanh toán thành công.
     * Topic: /topic/admin.notifications
     */
    public static NotificationEvent orderPaid(Integer orderId, java.math.BigDecimal amount) {
        return NotificationEvent.builder()
                .type("ORDER_PAID")
                .payload(Map.of(
                        "orderId", orderId,
                        "amount",  amount != null ? amount : java.math.BigDecimal.ZERO,
                        "status",  "COMPLETED"
                ))
                .build();
    }
}
