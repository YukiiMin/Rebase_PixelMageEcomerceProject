package com.example.PixelMageEcomerceProject.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitPaymentResult {
    private String gatewayTransactionId;
    private String paymentUrl;
    private boolean isRedirect;
    private String clientSecret; // For Stripe
}
