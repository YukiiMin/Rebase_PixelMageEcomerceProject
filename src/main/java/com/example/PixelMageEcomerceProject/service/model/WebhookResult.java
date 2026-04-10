package com.example.PixelMageEcomerceProject.service.model;

import java.math.BigDecimal;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResult {
    private boolean success;
    private PaymentStatus status;
    private String message;
    private Integer orderId;
    private BigDecimal amount;
    private String gatewayTransactionId;
}
