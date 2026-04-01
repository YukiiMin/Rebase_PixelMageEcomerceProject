package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.dto.response.PaymentResponseDTO;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface PaymentService {

    /**
     * Initialize payment using the active gateway.
     */
    InitPaymentResult initiatePayment(Integer orderId, BigDecimal amount, String currency);

    /**
     * Save payment record to database after successful payment.
     */
    Payment savePaymentRecord(Integer orderId, String gatewayTransactionId, PaymentGateway gateway, Map<String, Object> paymentData);

    /**
     * Update payment status.
     */
    Payment updatePaymentStatus(Integer paymentId, String status, String failureReason);

    /**
     * Get payment by order ID.
     */
    List<PaymentResponseDTO> getPaymentByOrderId(Integer orderId);

    /**
     * Get payment by gateway transaction ID.
     */
    Optional<Payment> getPaymentByGatewayTransactionId(String gatewayTransactionId);

    /**
     * Get customer's payment history.
     */
    List<PaymentResponseDTO> getCustomerPaymentHistory(Integer customerId);

    /**
     * Calculate processing fee (for display purposes).
     */
    BigDecimal calculateProcessingFee(BigDecimal amount);

    /**
     * Polling payment status (useful for SEPay VietQR).
     */
    PaymentStatus pollPaymentStatus(String gatewayTransactionId);
}
