package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.exceptions.PaymentNotFoundException;
import com.example.PixelMageEcomerceProject.exceptions.PaymentProcessingException;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.PaymentRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SetupIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final PackRepository packRepository;
    private final WebSocketNotificationService wsNotificationService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Override
    public PaymentIntent createPaymentIntent(Integer orderId, BigDecimal amount, String currency) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> PaymentNotFoundException.forOrderId(orderId));
            
            String customerId = getOrCreateStripeCustomerId(order.getAccount().getCustomerId());
            
            // Convert amount to cents (Stripe requires smallest currency unit)
            Long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setCustomer(customerId)
                    .putMetadata("order_id", orderId.toString())
                    .putMetadata("customer_id", order.getAccount().getCustomerId().toString())
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Save initial payment record
            savePaymentRecordFromIntent(order, paymentIntent, false);
            
            log.info("Created payment intent: {} for order: {}", paymentIntent.getId(), orderId);
            return paymentIntent;
            
        } catch (StripeException e) {
            log.error("Error creating payment intent for order {}: {}", orderId, e.getMessage());
            throw new PaymentProcessingException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    @Override
    public SetupIntent createSetupIntent(Integer customerId) {
        try {
            String stripeCustomerId = getOrCreateStripeCustomerId(customerId);
            
            SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                    .setCustomer(stripeCustomerId)
                    .putMetadata("customer_id", customerId.toString())
                    .setAutomaticPaymentMethods(
                        SetupIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                    )
                    .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
                    .build();

            SetupIntent setupIntent = SetupIntent.create(params);
            
            log.info("Created setup intent: {} for customer: {}", setupIntent.getId(), customerId);
            return setupIntent;
            
        } catch (StripeException e) {
            log.error("Error creating setup intent for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to create setup intent: " + e.getMessage());
        }
    }

    @Override
    public PaymentIntent confirmPaymentWithSavedCard(Integer orderId, String paymentMethodId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
            
            String customerId = getOrCreateStripeCustomerId(order.getAccount().getCustomerId());
            
            // Convert amount to cents
            Long amountInCents = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd") // Default to USD - can be made configurable
                    .setCustomer(customerId)
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .putMetadata("order_id", orderId.toString())
                    .putMetadata("customer_id", order.getAccount().getCustomerId().toString())
                    .setOffSession(true) // Indicates this is for saved payment method
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Save payment record
            savePaymentRecordFromIntent(order, paymentIntent, true);
            
            log.info("Confirmed payment with saved card: {} for order: {}", paymentIntent.getId(), orderId);
            return paymentIntent;
            
        } catch (StripeException e) {
            log.error("Error confirming payment with saved card for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to confirm payment with saved card: " + e.getMessage());
        }
    }

    @Override
    public Payment savePaymentRecord(Integer orderId, String stripePaymentIntentId, Map<String, Object> paymentData) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(stripePaymentIntentId);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
            
            // Check if payment record already exists
            Optional<Payment> existingPayment = paymentRepository.findByOrder_OrderId(orderId);
            Payment payment = existingPayment.orElse(new Payment());
            
            payment.setOrder(order);
            payment.setStripePaymentIntentId(stripePaymentIntentId);
            payment.setStripeCustomerId(paymentIntent.getCustomer());
            payment.setAmount(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
            payment.setCurrency(paymentIntent.getCurrency().toUpperCase());
            payment.setPaymentStatus(paymentIntent.getStatus().toUpperCase());
            payment.setClientSecret(paymentIntent.getClientSecret());
            
            if (paymentIntent.getPaymentMethod() != null) {
                payment.setStripePaymentMethodId(paymentIntent.getPaymentMethod());
            }
            
            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.setProcessedAt(LocalDateTime.now());
                payment.setNetAmount(payment.getAmount().subtract(calculateProcessingFee(payment.getAmount())));

                order.setPaymentStatus("PAID");
                updateOrderAndPacksOnPaymentSuccess(order);

                // Idempotency: mark webhook event as processed (24h TTL)
                String idempotencyKey = "payment:stripe:" + stripePaymentIntentId;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(idempotencyKey))) {
                    log.info("[PAYMENT] Duplicate webhook detected for PI: {} — skipping", stripePaymentIntentId);
                    return paymentRepository.save(payment);
                }
                redisTemplate.opsForValue().set(idempotencyKey, "processed", Duration.ofHours(24));

                // Push PAYMENT_CONFIRMED via WebSocket
                Integer customerId = order.getAccount().getCustomerId();
                wsNotificationService.pushToUser(customerId,
                        NotificationEvent.paymentConfirmed(customerId, orderId, stripePaymentIntentId));
            }

            return paymentRepository.save(payment);
            
        } catch (StripeException e) {
            log.error("Error saving payment record for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to save payment record: " + e.getMessage());
        }
    }

    @Override
    public Payment updatePaymentStatus(Integer paymentId, String status, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> PaymentNotFoundException.forPaymentId(paymentId));
        
        payment.setPaymentStatus(status);
        if (failureReason != null) {
            payment.setFailureReason(failureReason);
        }
        
        if ("SUCCEEDED".equals(status)) {
            payment.setProcessedAt(LocalDateTime.now());
            payment.setNetAmount(payment.getAmount().subtract(calculateProcessingFee(payment.getAmount())));

            Order order = payment.getOrder();
            order.setPaymentStatus("PAID");
            updateOrderAndPacksOnPaymentSuccess(order);

            // Push PAYMENT_CONFIRMED via WebSocket
            Integer customerId = order.getAccount().getCustomerId();
            wsNotificationService.pushToUser(customerId,
                    NotificationEvent.paymentConfirmed(customerId, order.getOrderId(),
                            payment.getStripePaymentIntentId()));
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentByOrderId(Integer orderId) {
        return paymentRepository.findByOrder_OrderId(orderId);
    }

    @Override
    public Optional<Payment> getPaymentByPaymentIntentId(String paymentIntentId) {
        return paymentRepository.findByStripePaymentIntentId(paymentIntentId);
    }

    @Override
    public List<PaymentMethod> getSavedPaymentMethods(Integer customerId) {
        try {
            String stripeCustomerId = getOrCreateStripeCustomerId(customerId);
            
            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();

            PaymentMethodCollection paymentMethods = PaymentMethod.list(params);
            
            log.info("Retrieved {} saved payment methods for customer: {}", paymentMethods.getData().size(), customerId);
            return paymentMethods.getData();
            
        } catch (StripeException e) {
            log.error("Error retrieving saved payment methods for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to retrieve saved payment methods: " + e.getMessage());
        }
    }

    @Override
    public List<Payment> getCustomerPaymentHistory(Integer customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    @Override
    public String getOrCreateStripeCustomerId(Integer customerId) {
        try {
            Account account = accountRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));
            
            // Check if customer already has a Stripe customer ID
            List<Payment> existingPayments = paymentRepository.findByCustomerId(customerId);
            if (!existingPayments.isEmpty() && existingPayments.get(0).getStripeCustomerId() != null) {
                return existingPayments.get(0).getStripeCustomerId();
            }
            
            // Create new Stripe customer
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(account.getEmail())
                    .setName(account.getName())
                    .putMetadata("customer_id", customerId.toString())
                    .build();

            Customer customer = Customer.create(params);
            
            log.info("Created Stripe customer: {} for account: {}", customer.getId(), customerId);
            return customer.getId();
            
        } catch (StripeException e) {
            log.error("Error creating Stripe customer for account {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage());
        }
    }

    @Override
    public void detachPaymentMethod(String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            paymentMethod.detach();
            
            log.info("Detached payment method: {}", paymentMethodId);
            
        } catch (StripeException e) {
            log.error("Error detaching payment method {}: {}", paymentMethodId, e.getMessage());
            throw new RuntimeException("Failed to detach payment method: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal calculateProcessingFee(BigDecimal amount) {
        // Stripe typical fee: 2.9% + $0.30 per transaction
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.029"));
        BigDecimal fixedFee = new BigDecimal("0.30");
        return percentageFee.add(fixedFee).setScale(2, RoundingMode.HALF_UP);
    }

    private void updateOrderAndPacksOnPaymentSuccess(Order order) {
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getPack() != null && "RESERVED".equals(item.getPack().getStatus())) {
                    Pack pack = item.getPack();
                    pack.setStatus("SOLD");
                    packRepository.save(pack);
                }
            });
        }
        order.setStatus("COMPLETED");
        orderRepository.save(order);
    }

    private Payment savePaymentRecordFromIntent(Order order, PaymentIntent paymentIntent, boolean isSavedPaymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStripeCustomerId(paymentIntent.getCustomer());
        payment.setAmount(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
        payment.setCurrency(paymentIntent.getCurrency().toUpperCase());
        payment.setPaymentStatus(paymentIntent.getStatus().toUpperCase());
        payment.setClientSecret(paymentIntent.getClientSecret());
        payment.setIsSavedPaymentMethod(isSavedPaymentMethod);
        
        if (paymentIntent.getPaymentMethod() != null) {
            payment.setStripePaymentMethodId(paymentIntent.getPaymentMethod());
        }
        
        return paymentRepository.save(payment);
    }
}