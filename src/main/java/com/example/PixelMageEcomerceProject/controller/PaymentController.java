package com.example.PixelMageEcomerceProject.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.PaymentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PaymentResponseDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import com.example.PixelMageEcomerceProject.service.model.WebhookResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.PixelMageEcomerceProject.mapper.PaymentMapper;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Gateway-agnostic payment management")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final Map<String, PaymentGatewayStrategy> strategies;
    private final PaymentMapper paymentMapper;

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Initiate payment", description = "Initialize payment using the active gateway (SEPay)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment initialized successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
    })
    public ResponseEntity<ResponseBase<InitPaymentResult>> initiatePayment(
            @RequestBody PaymentRequestDTO paymentRequestDTO,
            @Parameter(description = "Payment gateway (e.g., vnpay, sepay)") @RequestParam(defaultValue = "sepay") String gateway) {
        try {
            log.info("[PAYMENT] initiatePayment for order {} via {}", paymentRequestDTO.getOrderId(), gateway);
            PaymentGatewayStrategy strategy = strategies.get(gateway.toLowerCase());
            if (strategy == null) {
                return ResponseBase.error(HttpStatus.BAD_REQUEST, "Unsupported payment gateway: " + gateway);
            }

            InitPaymentResult result = strategy.initPayment(PaymentStrategyRequest.builder()
                    .orderId(paymentRequestDTO.getOrderId())
                    .amount(paymentRequestDTO.getAmount())
                    .currency(paymentRequestDTO.getCurrency() != null ? paymentRequestDTO.getCurrency() : "VND")
                    .description("Thanh toan don hang #" + paymentRequestDTO.getOrderId())
                    .build());

            return ResponseBase.ok(result, "Payment initialized successfully");
        } catch (Exception e) {
            log.error("[PAYMENT] Initiation failed", e);
            return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to initiate payment: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-payment")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Confirm payment", description = "Confirm payment and save record (Legacy support/Generic)")
    public ResponseEntity<ResponseBase<PaymentResponseDTO>> confirmPayment(
            @RequestBody Map<String, Object> paymentData) {
        try {
            Integer orderId = Integer.parseInt(paymentData.get("orderId").toString());
            String txId = paymentData.get("transactionId").toString();
            String gatewayStr = paymentData.getOrDefault("gateway", "SEPAY").toString();

            Payment savedPayment = paymentService.savePaymentRecord(orderId, txId,
                    PaymentGateway.valueOf(gatewayStr.toUpperCase()), paymentData);

            return ResponseBase.ok(paymentMapper.toPaymentResponseDTO(savedPayment), "Payment confirmed successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, "Payment confirmation failed: " + e.getMessage());
        }
    }

    @GetMapping("/history/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment history", description = "Retrieve customer's payment history")
    public ResponseEntity<ResponseBase<List<PaymentResponseDTO>>> getPaymentHistory(
            @Parameter(description = "Customer ID") @PathVariable Integer customerId) {
        try {
            List<PaymentResponseDTO> responseData = paymentService.getCustomerPaymentHistory(customerId);

            return ResponseBase.ok(responseData, "Payment history retrieved successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to retrieve history: " + e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment by order", description = "Retrieve payment information for a specific order")
    public ResponseEntity<ResponseBase<List<PaymentResponseDTO>>> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable Integer orderId) {
        try {
            List<PaymentResponseDTO> responseData = paymentService.getPaymentByOrderId(orderId);

            return ResponseBase.ok(responseData, "Payment info retrieved successfully");
        } catch (Exception e) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, "Payment not found: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/webhook/{gateway}", method = { RequestMethod.GET, RequestMethod.POST })
    @Operation(summary = "Generic Webhook", description = "Generic endpoint to receive payment notifications from various gateways")
    public ResponseEntity<Object> handleWebhook(
            @PathVariable String gateway,
            HttpServletRequest request) {
        log.info("[WEBHOOK] Received notification from gateway: {}", gateway);

        PaymentGatewayStrategy strategy = strategies.get(gateway.toLowerCase());
        if (strategy == null) {
            return ResponseEntity.badRequest().body("Unsupported gateway: " + gateway);
        }

        Map<String, String> payload = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            payload.put(paramName, request.getParameter(paramName));
        }

        WebhookResult result = strategy.handleWebhook(payload);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getMessage());
        }
    }
}
