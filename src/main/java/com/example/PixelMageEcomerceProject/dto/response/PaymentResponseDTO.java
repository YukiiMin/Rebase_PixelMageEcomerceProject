package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Integer paymentId;
    private Integer orderId;
    private String gatewayTransactionId;
    private String paymentGateway;
    private String paymentStatus;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private BigDecimal processingFee;
    private BigDecimal netAmount;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}