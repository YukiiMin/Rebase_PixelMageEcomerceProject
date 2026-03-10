package com.example.PixelMageEcomerceProject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.config.PaymentConfig;
import com.example.PixelMageEcomerceProject.dto.request.PaymentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.SavedCardPaymentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ClientSecretResponseDTO;
import com.example.PixelMageEcomerceProject.dto.response.PaymentResponseDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.dto.response.SavedPaymentMethodDTO;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing Stripe payments and saved payment methods")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

        private final PaymentService paymentService;
        private final PaymentConfig paymentConfig;

        @PostMapping("/create-payment-intent")
        @Operation(summary = "Create payment intent", description = "Create a Stripe payment intent for one-time payment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment intent created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ClientSecretResponseDTO>> createPaymentIntent(
                        @RequestBody PaymentRequestDTO paymentRequestDTO) {
                try {
                        PaymentIntent paymentIntent = paymentService.createPaymentIntent(
                                        paymentRequestDTO.getOrderId(),
                                        paymentRequestDTO.getAmount(),
                                        paymentRequestDTO.getCurrency());

                        ClientSecretResponseDTO responseData = new ClientSecretResponseDTO(
                                        paymentIntent.getClientSecret(),
                                        paymentIntent.getId(),
                                        null,
                                        paymentConfig.getStripePublicKey());

                        return ResponseBase.ok(responseData, "Payment intent created successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create payment intent: " + e.getMessage());
                }
        }

        @PostMapping("/create-setup-intent")
        @Operation(summary = "Create setup intent", description = "Create a Stripe setup intent for saving payment method")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Setup intent created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<ClientSecretResponseDTO>> createSetupIntent(
                        @Parameter(description = "Customer ID") @RequestParam Integer customerId) {
                try {
                        SetupIntent setupIntent = paymentService.createSetupIntent(customerId);

                        ClientSecretResponseDTO responseData = new ClientSecretResponseDTO(
                                        setupIntent.getClientSecret(),
                                        null,
                                        setupIntent.getId(),
                                        paymentConfig.getStripePublicKey());

                        return ResponseBase.ok(responseData, "Setup intent created successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create setup intent: " + e.getMessage());
                }
        }

        @PostMapping("/pay-with-saved-card")
        @Operation(summary = "Pay with saved card", description = "Process payment using a saved payment method")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment processed successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Payment failed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PaymentResponseDTO>> payWithSavedCard(
                        @RequestBody SavedCardPaymentRequestDTO requestDTO) {
                try {
                        PaymentIntent paymentIntent = paymentService.confirmPaymentWithSavedCard(
                                        requestDTO.getOrderId(),
                                        requestDTO.getPaymentMethodId());

                        Map<String, Object> paymentData = new HashMap<>();
                        Payment savedPayment = paymentService.savePaymentRecord(
                                        requestDTO.getOrderId(),
                                        paymentIntent.getId(),
                                        paymentData);

                        PaymentResponseDTO responseData = convertToPaymentResponseDTO(savedPayment);

                        return ResponseBase.ok(responseData, "Payment processed successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, "Payment failed: " + e.getMessage());
                }
        }

        @PostMapping("/confirm-payment/{paymentIntentId}")
        @Operation(summary = "Confirm payment", description = "Confirm payment and save payment record")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment confirmed successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Payment confirmation failed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PaymentResponseDTO>> confirmPayment(
                        @Parameter(description = "Stripe payment intent ID") @PathVariable String paymentIntentId,
                        @RequestBody Map<String, Object> paymentData) {
                try {
                        // Extract order ID from payment data or metadata
                        Integer orderId = Integer.parseInt((String) paymentData.get("orderId"));

                        Payment savedPayment = paymentService.savePaymentRecord(orderId, paymentIntentId, paymentData);
                        PaymentResponseDTO responseData = convertToPaymentResponseDTO(savedPayment);

                        return ResponseBase.ok(responseData, "Payment confirmed successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Payment confirmation failed: " + e.getMessage());
                }
        }

        @GetMapping("/saved-payment-methods/{customerId}")
        @Operation(summary = "Get saved payment methods", description = "Retrieve customer's saved payment methods")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Saved payment methods retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Failed to retrieve payment methods", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<SavedPaymentMethodDTO>>> getSavedPaymentMethods(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId) {
                try {
                        List<PaymentMethod> paymentMethods = paymentService.getSavedPaymentMethods(customerId);

                        List<SavedPaymentMethodDTO> responseData = paymentMethods.stream()
                                        .map(this::convertToSavedPaymentMethodDTO)
                                        .collect(Collectors.toList());

                        return ResponseBase.ok(responseData, "Saved payment methods retrieved successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to retrieve saved payment methods: " + e.getMessage());
                }
        }

        @GetMapping("/history/{customerId}")
        @Operation(summary = "Get payment history", description = "Retrieve customer's payment history")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Failed to retrieve payment history", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<PaymentResponseDTO>>> getPaymentHistory(
                        @Parameter(description = "Customer ID") @PathVariable Integer customerId) {
                try {
                        List<Payment> payments = paymentService.getCustomerPaymentHistory(customerId);

                        List<PaymentResponseDTO> responseData = payments.stream()
                                        .map(this::convertToPaymentResponseDTO)
                                        .collect(Collectors.toList());

                        return ResponseBase.ok(responseData, "Payment history retrieved successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to retrieve payment history: " + e.getMessage());
                }
        }

        @DeleteMapping("/detach-payment-method/{paymentMethodId}")
        @Operation(summary = "Remove saved payment method", description = "Detach a saved payment method from customer")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment method removed successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Failed to remove payment method", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> detachPaymentMethod(
                        @Parameter(description = "Stripe payment method ID") @PathVariable String paymentMethodId) {
                try {
                        paymentService.detachPaymentMethod(paymentMethodId);
                        return ResponseBase.ok(null, "Payment method removed successfully");
                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to remove payment method: " + e.getMessage());
                }
        }

        @GetMapping("/order/{orderId}")
        @Operation(summary = "Get payment by order", description = "Retrieve payment information for a specific order")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment information retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Payment not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<PaymentResponseDTO>> getPaymentByOrderId(
                        @Parameter(description = "Order ID") @PathVariable Integer orderId) {
                try {
                        Payment payment = paymentService.getPaymentByOrderId(orderId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Payment not found for order: " + orderId));

                        PaymentResponseDTO responseData = convertToPaymentResponseDTO(payment);

                        return ResponseBase.ok(responseData, "Payment information retrieved successfully");

                } catch (Exception e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Payment not found: " + e.getMessage());
                }
        }

        private PaymentResponseDTO convertToPaymentResponseDTO(Payment payment) {
                return new PaymentResponseDTO(
                                payment.getPaymentId(),
                                payment.getOrder().getOrderId(),
                                payment.getStripePaymentIntentId(),
                                payment.getPaymentStatus(),
                                payment.getAmount(),
                                payment.getCurrency(),
                                payment.getPaymentMethod(),
                                payment.getProcessingFee(),
                                payment.getNetAmount(),
                                payment.getFailureReason(),
                                payment.getCreatedAt(),
                                payment.getProcessedAt());
        }

        private SavedPaymentMethodDTO convertToSavedPaymentMethodDTO(PaymentMethod paymentMethod) {
                PaymentMethod.Card card = paymentMethod.getCard();
                return new SavedPaymentMethodDTO(
                                paymentMethod.getId(),
                                card != null ? card.getBrand() : null,
                                card != null ? card.getLast4() : null,
                                card != null ? card.getExpMonth() : null,
                                card != null ? card.getExpYear() : null,
                                card != null ? card.getFingerprint() : null,
                                false // Default flag, can be enhanced later
                );
        }
}
