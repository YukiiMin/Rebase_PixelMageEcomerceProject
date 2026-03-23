package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.exceptions.PaymentNotFoundException;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.PaymentRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PackRepository packRepository;
    private final WebSocketNotificationService wsNotificationService;
    @Qualifier("sepay")
    private final PaymentGatewayStrategy activeGateway;

    // // PaymentServiceImpl.java
    // public PaymentServiceImpl(PaymentRepository paymentRepository,
    // OrderRepository orderRepository,
    // PackRepository packRepository,
    // WebSocketNotificationService wsNotificationService,
    // @Qualifier("sepay") PaymentGatewayStrategy activeGateway) {
    // this.paymentRepository = paymentRepository;
    // this.orderRepository = orderRepository;
    // this.packRepository = packRepository;
    // this.wsNotificationService = wsNotificationService;
    // this.activeGateway = activeGateway;
    // }

    @Override
    @Transactional
    public InitPaymentResult initiatePayment(Integer orderId, BigDecimal amount, String currency) {
        log.info("Initiating payment for orderId: {}, amount: {}, currency: {}", orderId, amount, currency);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> PaymentNotFoundException.forOrderId(orderId));

        PaymentStrategyRequest request = PaymentStrategyRequest.builder()
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .description("Payment for Order #" + orderId)
                .build();

        InitPaymentResult result = activeGateway.initPayment(request);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentGateway(
                PaymentGateway.valueOf(activeGateway.getClass().getSimpleName().replace("Gateway", "").toUpperCase()));
        payment.setGatewayTransactionId(result.getGatewayTransactionId());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        return result;
    }

    @Override
    @Transactional
    public Payment savePaymentRecord(Integer orderId, String gatewayTransactionId, PaymentGateway gateway,
            Map<String, Object> paymentData) {
        log.info("Saving payment record for orderId: {}, gatewayTransactionId: {}, gateway: {}", orderId,
                gatewayTransactionId, gateway);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> PaymentNotFoundException.forOrderId(orderId));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("VND");
        payment.setPaymentGateway(gateway);
        payment.setGatewayTransactionId(gatewayTransactionId);
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        payment.setProcessedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Integer paymentId, String status, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus(PaymentStatus.valueOf(status));
        payment.setFailureReason(failureReason);
        payment.setProcessedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getPaymentByOrderId(Integer orderId) {
        return paymentRepository.findByOrder_OrderId(orderId);
    }

    @Override
    public Optional<Payment> getPaymentByGatewayTransactionId(String gatewayTransactionId) {
        return paymentRepository.findByGatewayTransactionId(gatewayTransactionId);
    }

    @Override
    public List<Payment> getCustomerPaymentHistory(Integer customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    @Override
    public BigDecimal calculateProcessingFee(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.02"));
    }

    @Override
    public PaymentStatus pollPaymentStatus(String gatewayTransactionId) {
        return activeGateway.pollStatus(gatewayTransactionId);
    }
}
