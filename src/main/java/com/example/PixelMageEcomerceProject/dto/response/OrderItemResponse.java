package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Integer orderItemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private PackSummaryResponse pack;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackSummaryResponse {
        private Integer packId;
        private String status;
        private ProductSummaryResponse product;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummaryResponse {
        private Integer productId;
        private String name;
        private String imageUrl;
    }
}
