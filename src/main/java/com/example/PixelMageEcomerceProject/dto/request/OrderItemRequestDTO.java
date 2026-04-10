package com.example.PixelMageEcomerceProject.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDTO {
    private Integer orderId;
    /** The product the customer wants to buy. Pack will be assigned after payment. */
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String customText;
}
