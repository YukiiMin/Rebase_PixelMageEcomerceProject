package com.example.PixelMageEcomerceProject.service.model;

import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResult {
    private boolean success;
    private PaymentStatus status;
    private String message;
}
