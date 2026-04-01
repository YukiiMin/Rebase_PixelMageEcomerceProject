package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer orderId;
    private LocalDateTime orderDate;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String shippingAddress;
    private String notes;
    private LocalDateTime createdAt;
    
    private AccountSummaryResponse customer;
    private List<OrderItemResponse> orderItems;
    private AppliedVoucherResponse appliedVoucher;
    private String paymentQrUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedVoucherResponse {
        private String code;
        private Integer discountPct;
        private Integer maxDiscountVnd;
    }
}
