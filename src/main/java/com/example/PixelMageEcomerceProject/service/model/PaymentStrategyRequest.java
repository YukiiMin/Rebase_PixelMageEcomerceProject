package com.example.PixelMageEcomerceProject.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStrategyRequest {
    private Integer orderId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String ipAddress;
}
