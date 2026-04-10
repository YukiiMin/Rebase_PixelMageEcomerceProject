package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import com.example.PixelMageEcomerceProject.service.model.WebhookResult;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("sepay")
@Slf4j
@Primary
public class SEPayGatewayimpl implements PaymentGatewayStrategy {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${sepay.bank-account:0703376647}")
    private String bankAccount;

    @Value("${sepay.bank-code:MB}")
    private String bankCode;

    @Override
    public InitPaymentResult initPayment(PaymentStrategyRequest request) {
        log.info("[SEPay] initPayment for order {}", request.getOrderId());

        // Sử dụng tài khoản chính và addInfo động (PIXELMAGE_ORD_ID)
        String addInfo = "PIXELMAGE_ORD_" + request.getOrderId();
        String vietQrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact.png?amount=%s&addInfo=%s",
            bankCode,
            bankAccount,
            request.getAmount().toBigInteger(),
            addInfo
        );

        return InitPaymentResult.builder()
                .paymentUrl(vietQrUrl)
                .isRedirect(false)
                .gatewayTransactionId("SEPAY_" + request.getOrderId() + "_" + System.currentTimeMillis())
                .build();
    }


    public SEPayGatewayimpl(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public WebhookResult handleWebhook(Map<String, String> payload) {
        String content = payload.getOrDefault("content", "");
        String amountStr = payload.getOrDefault("transferAmount", "0");
        String sepayId = payload.getOrDefault("id", "unknown");

        log.info("[SEPay] Processing webhook: content={}, amount={}, id={}", content, amountStr, sepayId);

        // ID đơn hàng từ content
        // Ngân hàng thường bỏ dấu _ nên: PIXELMAGE_ORD_6 → PIXELMAGEORD6
        // Regex hỗ trợ cả 2 dạng: PIXELMAGE_ORD_6, PIXELMAGEORD6, ORD_6, ORD6
        Integer orderId = null;
        try {
            String upperContent = content.toUpperCase();
            java.util.regex.Matcher matcher;

            // Pattern 1: PIXELMAGE_ORD_6 hoặc PIXELMAGEORD6
            matcher = java.util.regex.Pattern.compile("PIXELMAGE[_]?ORD[_]?(\\d+)").matcher(upperContent);
            if (matcher.find()) {
                orderId = Integer.parseInt(matcher.group(1));
            }

            // Pattern 2: dự phòng ORD_6 hoặc ORD6
            if (orderId == null) {
                matcher = java.util.regex.Pattern.compile("\\bORD[_]?(\\d+)").matcher(upperContent);
                if (matcher.find()) {
                    orderId = Integer.parseInt(matcher.group(1));
                }
            }

            log.info("[SEPay] Parsed orderId={} from content: {}", orderId, content);
        } catch (Exception e) {
            log.error("[SEPay] Failed to parse orderId from content: {}", content);
        }

        if (sepayId != null) {
            String idempotencyKey = "sepay:webhook:" + sepayId;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "processed", java.time.Duration.ofHours(24));

            if (Boolean.FALSE.equals(isNew)) {
                log.info("[SEPay] Duplicate webhook detected for SEPay ID: {} — skipping", sepayId);
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
                .orderId(orderId)
                .amount(new java.math.BigDecimal(amountStr))
                .gatewayTransactionId(sepayId)
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

    @Override
    public PaymentGateway getGatewayType() {
        return PaymentGateway.SEPAY;
    }
}
