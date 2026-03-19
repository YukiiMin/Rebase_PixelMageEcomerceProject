package com.example.PixelMageEcomerceProject.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface VNPayService {

    /**
     * Creates a VNPAY payment URL for the given order ID and amount.
     */
    String createPaymentUrl(Integer orderId, int amount, String orderInfo, String ipAddress);

    /**
     * Processes the return callback from VNPAY.
     * Validates the checksum and updates the order/payment status.
     * Returns true if payment was successful, false otherwise.
     */
    boolean processPaymentReturn(HttpServletRequest request);

    /**
     * Processes the IPN (Instant Payment Notification) from VNPAY.
     * Validates the checksum, handles idempotency, and updates the order/payment status.
     * Returns a Map with RspCode and Message for VNPAY.
     */
    Map<String, String> processIpn(HttpServletRequest request);
}
