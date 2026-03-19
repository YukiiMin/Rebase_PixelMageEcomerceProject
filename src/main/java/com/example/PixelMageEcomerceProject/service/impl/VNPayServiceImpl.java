package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.config.VNPayConfig;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.PaymentRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.VNPayService;

import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PackRepository packRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        String signValue = VNPayConfig.hashAllFields(fields, vnPayConfig.getVnp_HashSecret());

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                // Payment success
                String txnRef = request.getParameter("vnp_TxnRef"); // Example: 1-12345678
                String[] parts = txnRef.split("-");
                if (parts.length >= 1) {
                    try {
                        Integer orderId = Integer.parseInt(parts[0]);
                        Order order = orderRepository.findById(orderId).orElse(null);
                        if (order != null && PaymentStatus.PENDING.equals(order.getPaymentStatus())) {
                            updateOrderAndPacksOnPaymentSuccess(order);

                            // Save payment record
                            savePaymentRecord(order, request.getParameter("vnp_TransactionNo"), request.getParameter("vnp_Amount"));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            } else {
                // Payment Failure from VNPAY
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public Map<String, String> processIpn(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = VNPayConfig.hashAllFields(fields, vnPayConfig.getVnp_HashSecret());

        if (!signValue.equals(vnp_SecureHash)) {
            result.put("RspCode", "97");
            result.put("Message", "Invalid Checksum");
            return result;
        }

        String txnRef = request.getParameter("vnp_TxnRef");
        Integer orderId = Integer.parseInt(txnRef.split("-")[0]);
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            result.put("RspCode", "01");
            result.put("Message", "Order not found");
            return result;
        }

        // Idempotency check
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String idempotencyKey = "payment:vnpay:" + transactionNo;

        try {
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "processed", Duration.ofHours(24));
            if (Boolean.FALSE.equals(isNew)) {
                log.info("[VNPAY] Duplicate IPN detected for TransactionNo: {} — skipping", transactionNo);
                result.put("RspCode", "00");
                result.put("Message", "Confirm Success");
                return result;
            }
        } catch (Exception e) {
            log.error("[VNPAY] Redis unavailable during idempotency check: {}", e.getMessage());
            throw new RedisUnavailableException("Redis unavailable for idempotency check");
        }

        if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
            if (PaymentStatus.PENDING.equals(order.getPaymentStatus())) {
                updateOrderAndPacksOnPaymentSuccess(order);
                savePaymentRecord(order, transactionNo, request.getParameter("vnp_Amount"));
                result.put("RspCode", "00");
                result.put("Message", "Confirm Success");
            } else {
                result.put("RspCode", "02");
                result.put("Message", "Order already confirmed");
            }
        } else {
            result.put("RspCode", "00");
            result.put("Message", "Confirm Success"); // VNPAY expects 00 even for fail response to acknowledge IPN
        }

        return result;
    }

    private void updateOrderAndPacksOnPaymentSuccess(Order order) {
        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        order.setStatus(OrderStatus.PROCESSING);

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getPack() != null && PackStatus.RESERVED.equals(item.getPack().getStatus())) {
                    Pack pack = item.getPack();
                    pack.setStatus(PackStatus.SOLD);
                    packRepository.save(pack);
                }
            });
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    private void savePaymentRecord(Order order, String transactionNo, String amountStr) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStripePaymentIntentId("VNPAY_" + transactionNo);
        payment.setAmount(new BigDecimal(amountStr).divide(new BigDecimal(100)));
        payment.setCurrency("VND");
        payment.setPaymentMethod("VNPAY");
        payment.setPaymentStatus(PaymentStatus.SUCCEEDED);
        paymentRepository.save(payment);
    }
}
