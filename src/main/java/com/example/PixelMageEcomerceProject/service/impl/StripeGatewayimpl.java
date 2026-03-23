package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import com.example.PixelMageEcomerceProject.service.model.WebhookResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "payment.gateway.active", havingValue = "stripe")
@Slf4j
public class StripeGatewayimpl implements PaymentGatewayStrategy {

    @Override
    public InitPaymentResult initPayment(PaymentStrategyRequest request) {
        log.info("[Stripe] initPayment for order {}", request.getOrderId());
        return InitPaymentResult.builder()
                .gatewayTransactionId("STRIPE_" + request.getOrderId())
                .clientSecret("pi_mock_secret")
                .isRedirect(false)
                .build();
    }

    @Override
    public WebhookResult handleWebhook(Map<String, String> payload) {
        log.info("[Stripe] handleWebhook called");
        return WebhookResult.builder()
                .success(true)
                .status(PaymentStatus.SUCCEEDED)
                .message("Processed via Stripe")
                .build();
    }

    @Override
    public boolean verifySignature(Map<String, String> payload) {
        return true;
    }

    @Override
    public PaymentStatus pollStatus(String gatewayTransactionId) {
        return PaymentStatus.PENDING;
    }
}
