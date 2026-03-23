package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import com.example.PixelMageEcomerceProject.service.model.WebhookResult;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("sepay")
@Slf4j
@Primary
public class SEPayGatewayimpl implements PaymentGatewayStrategy {

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public InitPaymentResult initPayment(PaymentStrategyRequest request) {
        log.info("[SEPay] initPayment for order {}", request.getOrderId());
        // For SEPay, we don't return a specific URL. The FE generates vietQR directly,
        // or BE can return the VietQR string/URL.
        return InitPaymentResult.builder()
                .paymentUrl("sepay_qr_stand_in_url") // Normally generate VietQR url here if needed
                .isRedirect(false)
                .gatewayTransactionId("SEPAY_" + request.getOrderId() + "_" + System.currentTimeMillis())
                .build();
    }


    public SEPayGatewayimpl(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public WebhookResult handleWebhook(Map<String, String> payload) {
        log.info("[SEPay] handleWebhook called with payload: {}", payload);

        String sepayReferenceId = payload.getOrDefault("reference_number", payload.get("id"));
        if (sepayReferenceId != null) {
            String idempotencyKey = "sepay:webhook:" + sepayReferenceId;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "processed", java.time.Duration.ofHours(24));

            if (Boolean.FALSE.equals(isNew)) {
                log.info("[SEPay] Duplicate webhook detected for SEPay Reference ID: {} — skipping", sepayReferenceId);
                return WebhookResult.builder()
                        .success(true)
                        .status(PaymentStatus.SUCCEEDED)
                        .message("Duplicate webhook skipped")
                        .build();
            }
        }

        return WebhookResult.builder()
                .success(true)
                .status(PaymentStatus.SUCCEEDED)
                .message("Processed via SEPay")
                .build();
    }

    @Override
    public boolean verifySignature(Map<String, String> payload) {
        // Implement SEPay signature check
        return true;
    }

    @Override
    public PaymentStatus pollStatus(String gatewayTransactionId) {
        // Here we could call SEPay API to check if bank transfer is received for this transaction.
        log.info("[SEPay] pollStatus for transaction {}", gatewayTransactionId);
        return PaymentStatus.PENDING;
    }
}
