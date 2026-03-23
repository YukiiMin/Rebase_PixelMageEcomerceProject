package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Find payment by order ID.
     */
    List<Payment> findByOrder_OrderId(Integer orderId);

    /**
     * Find payment by Gateway Transaction ID.
     */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    /**
     * Find payment by Stripe customer ID.
     */
    List<Payment> findByStripeCustomerId(String stripeCustomerId);

    /**
     * Find payments by customer account ID.
     */
    @Query("SELECT p FROM Payment p WHERE p.order.account.customerId = :customerId")
    List<Payment> findByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Find payments by payment status.
     */
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Find saved payment methods by customer ID.
     */
    @Query("SELECT p FROM Payment p WHERE p.order.account.customerId = :customerId AND p.isSavedPaymentMethod = true")
    List<Payment> findSavedPaymentMethodsByCustomerId(@Param("customerId") Integer customerId);
}
