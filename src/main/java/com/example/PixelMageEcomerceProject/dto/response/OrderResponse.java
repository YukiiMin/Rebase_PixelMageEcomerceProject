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
    
    private AccountResponse.Summary customer;
    private List<Item> orderItems;
    private AppliedVoucher appliedVoucher;
    private String paymentQrUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Integer orderItemId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String customText;
        /** Product that was ordered — always present */
        private ProductSummary product;
        /** Physical Pack assigned after payment — null until payment succeeds */
        private PackSummary pack;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackSummary {
        private Integer packId;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private Integer productId;
        private String name;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedVoucher {
        private String code;
        private Integer discountPct;
        private Integer maxDiscountVnd;
    }
}
