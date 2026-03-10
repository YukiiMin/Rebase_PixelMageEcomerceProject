package com.example.PixelMageEcomerceProject.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.PixelMageEcomerceProject.config.VNPayConfig;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.service.interfaces.VNPayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
@Tag(name = "VNPay Management", description = "APIs for VNPay integration")
public class VNPayController {

    private final VNPayService vnPayService;

    @PostMapping("/create-payment")
    @Operation(summary = "Create VNPay payment URL", description = "Generate VNPay redirect URL for payment")
    public ResponseEntity<ResponseBase<Map<String, String>>> createPayment(
            @RequestParam Integer orderId,
            @RequestParam int amount,
            @RequestParam(required = false, defaultValue = "Thanh toan don hang PixelMage") String orderInfo,
            HttpServletRequest request) {

        try {
            String ipAddress = VNPayConfig.getIpAddress(request);
            String paymentUrl = vnPayService.createPaymentUrl(orderId, amount, orderInfo, ipAddress);

            Map<String, String> result = new HashMap<>();
            result.put("paymentUrl", paymentUrl);

            return ResponseBase.ok(result, "VNPay payment URL created successfully");

        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to create VNPay payment URL: " + e.getMessage());
        }
    }

    @GetMapping("/payment-return")
    @Operation(summary = "Handle VNPay return", description = "Callback endpoint for VNPay after payment")
    public RedirectView paymentReturn(HttpServletRequest request) {
        boolean isSuccess = false;
        try {
            isSuccess = vnPayService.processPaymentReturn(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ideally, redirect to a frontend page with the result
        // For development, we redirect to localhost:3000
        String redirectUrl = "http://localhost:3000/payment-result?success=" + isSuccess;
        return new RedirectView(redirectUrl);
    }
}
