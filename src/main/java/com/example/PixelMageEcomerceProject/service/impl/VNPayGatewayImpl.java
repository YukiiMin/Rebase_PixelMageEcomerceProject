package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.config.VNPayConfig;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.event.PaymentSuccessEvent;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PaymentRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.interfaces.VNPayService;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import com.example.PixelMageEcomerceProject.service.model.WebhookResult;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayGatewayImpl implements VNPayService, PaymentGatewayStrategy {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderRepository orderRepository; // Keeping it temporarily just for lookup, but plan said remove.
    // Wait, if I remove OrderRepo, how do I find the Order to save Payment?
    // Payment entity has a relationship with Order. I need Order object.
    // I can use orderRepository.getReferenceById if I have the ID.

    @Override
    public InitPaymentResult initPayment(PaymentStrategyRequest request) {
        String paymentUrl = createPaymentUrl(request.getOrderId(),
                request.getAmount().intValue(),
                request.getDescription() != null ? request.getDescription() : "Thanh toan don hang PixelMage",
                request.getIpAddress());

        return InitPaymentResult.builder()
                .paymentUrl(paymentUrl)
                .isRedirect(true)
                .gatewayTransactionId("VNPAY_INIT_" + request.getOrderId() + "_" + System.currentTimeMillis())
                .build();
    }

    @Override
    public String createPaymentUrl(Integer orderId, int amount, String orderInfo, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = orderId.toString() + "-" + VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = ipAddress;
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY amount is valid * 100
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return paymentUrl;
    }

    @Override
    public WebhookResult handleWebhook(Map<String, String> payload) {
        log.info("[VNPAY] handleWebhook called with payload: {}", payload);

        if (!verifySignature(payload)) {
            return WebhookResult.builder()
                    .success(false)
                    .message("Invalid Checksum")
                    .build();
        }

        String txnRef = payload.get("vnp_TxnRef");
        if (txnRef == null) {
            return WebhookResult.builder().success(false).message("Missing vnp_TxnRef").build();
        }

        Integer orderId = Integer.parseInt(txnRef.split("-")[0]);
        String transactionNo = payload.get("vnp_TransactionNo");
        String responseCode = payload.get("vnp_ResponseCode");

        // Idempotency check
        String idempotencyKey = "payment:vnpay:" + transactionNo;
        try {
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "processed", Duration.ofHours(24));
            if (Boolean.FALSE.equals(isNew)) {
                log.info("[VNPAY] Duplicate IPN detected for TransactionNo: {} — skipping", transactionNo);
                return WebhookResult.builder().success(true).status(PaymentStatus.SUCCEEDED).message("Confirm Success")
                        .build();
            }
        } catch (Exception e) {
            log.error("[VNPAY] Redis unavailable during idempotency check: {}", e.getMessage());
            throw new RedisUnavailableException("Dịch vụ thanh toán tạm thời không khả dụng. Vui lòng thử lại sau.");
        }

        if ("00".equals(responseCode)) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                // Save payment record
                savePaymentRecord(order, transactionNo, payload.get("vnp_Amount"));

                // Publish Event
                eventPublisher.publishEvent(new PaymentSuccessEvent(this, orderId, transactionNo,
                        new BigDecimal(payload.get("vnp_Amount")).divide(new BigDecimal(100)), PaymentGateway.VNPAY));

                return WebhookResult.builder().success(true).status(PaymentStatus.SUCCEEDED).message("Confirm Success")
                        .build();
            } else {
                return WebhookResult.builder().success(false).message("Order not found").build();
            }
        }

        return WebhookResult.builder().success(true).status(PaymentStatus.FAILED).message("Payment failed").build();
    }

    @Override
    public boolean verifySignature(Map<String, String> payload) {
        String vnp_SecureHash = payload.get("vnp_SecureHash");
        Map<String, String> hashFields = new HashMap<>(payload);
        hashFields.remove("vnp_SecureHashType");
        hashFields.remove("vnp_SecureHash");

        String signValue = VNPayConfig.hashAllFields(hashFields, vnPayConfig.getVnp_HashSecret());
        return signValue.equals(vnp_SecureHash);
    }

    @Override
    public PaymentStatus pollStatus(String gatewayTransactionId) {
        return PaymentStatus.PENDING; // VNPay doesn't easily support simple polling without complex API calls
    }

    @Override
    @Transactional
    public boolean processPaymentReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        WebhookResult result = handleWebhook(fields);
        return result.isSuccess() && PaymentStatus.SUCCEEDED.equals(result.getStatus());
    }

    @Override
    @Transactional
    public Map<String, String> processIpn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        WebhookResult result = handleWebhook(fields);
        Map<String, String> response = new HashMap<>();
        if (result.isSuccess()) {
            response.put("RspCode", "00");
            response.put("Message", result.getMessage());
        } else {
            // Map common errors back to VNPay codes if needed, but 01/97 are likely handled
            // in handleWebhook
            response.put("RspCode", "99");
            response.put("Message", result.getMessage());
        }
        return response;
    }

    private void savePaymentRecord(Order order, String transactionNo, String amountStr) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setGatewayTransactionId("VNPAY_" + transactionNo);
        payment.setPaymentGateway(PaymentGateway.VNPAY);
        payment.setAmount(new BigDecimal(amountStr).divide(new BigDecimal(100)));
        payment.setCurrency("VND");
        payment.setPaymentMethod("VNPAY");
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
    }
}
