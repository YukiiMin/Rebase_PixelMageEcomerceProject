package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.entity.Payment;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface PaymentService {
    
    /**
     * Create a payment intent for one-time payment.
     */
    PaymentIntent createPaymentIntent(Integer orderId, BigDecimal amount, String currency);
    
    /**
     * Create a setup intent for saving payment method.
     */
    SetupIntent createSetupIntent(Integer customerId);
    
    /**
     * Confirm payment with existing payment method.
     */
    PaymentIntent confirmPaymentWithSavedCard(Integer orderId, String paymentMethodId);
    
    /**
     * Save payment record to database after successful payment.
     */
    Payment savePaymentRecord(Integer orderId, String stripePaymentIntentId, Map<String, Object> paymentData);
    
    /**
     * Update payment status.
     */
    Payment updatePaymentStatus(Integer paymentId, String status, String failureReason);
    
    /**
     * Get payment by order ID.
     */
    List<Payment> getPaymentByOrderId(Integer orderId);
    
    /**
     * Get payment by payment intent ID.
     */
    Optional<Payment> getPaymentByPaymentIntentId(String paymentIntentId);
    
    /**
     * Get customer's saved payment methods.
     */
    List<PaymentMethod> getSavedPaymentMethods(Integer customerId);
    
    /**
     * Get customer's payment history.
     */
    List<Payment> getCustomerPaymentHistory(Integer customerId);
    
    /**
     * Create or retrieve Stripe customer.
     */
    String getOrCreateStripeCustomerId(Integer customerId);
    
    /**
     * Detach payment method from customer.
     */
    void detachPaymentMethod(String paymentMethodId);
    
    /**
     * Calculate processing fee (for display purposes).
     */
    BigDecimal calculateProcessingFee(BigDecimal amount);
}