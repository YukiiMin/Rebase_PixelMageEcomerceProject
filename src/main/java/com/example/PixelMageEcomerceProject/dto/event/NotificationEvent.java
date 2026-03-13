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
    private LocalDateTime timestamp = LocalDateTime.now();

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
}
