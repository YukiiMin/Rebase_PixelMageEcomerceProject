package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import com.example.PixelMageEcomerceProject.service.model.WebhookResult;

import java.util.Map;

/**
 * Strategy interface for payment gateway integrations.
 * Active gateway is selected via application.properties: payment.gateway.active
 */
public interface PaymentGatewayStrategy {
    InitPaymentResult initPayment(PaymentStrategyRequest request);
    WebhookResult handleWebhook(Map<String, String> payload);
    boolean verifySignature(Map<String, String> payload);
    PaymentStatus pollStatus(String gatewayTransactionId);
}
