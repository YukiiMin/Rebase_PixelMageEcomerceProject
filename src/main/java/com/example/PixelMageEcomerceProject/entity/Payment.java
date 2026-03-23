package com.example.PixelMageEcomerceProject.entity;

import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity with 1-1 relationship to Order.
 * Stores Stripe payment details and transaction history.
 */
@Entity
@Table(name = "PAYMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, referencedColumnName = "order_id")
    @JsonBackReference("order-payment")
    private Order order;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway", length = 50)
    private PaymentGateway paymentGateway;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "stripe_payment_method_id", length = 100)
    private String stripePaymentMethodId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // USD, VND, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus; // PENDING, SUCCEEDED, FAILED, CANCELED, REQUIRES_ACTION

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // CARD, BANK_TRANSFER, etc.

    @Column(name = "client_secret", length = 200)
    private String clientSecret;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "is_saved_payment_method")
    private Boolean isSavedPaymentMethod = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
