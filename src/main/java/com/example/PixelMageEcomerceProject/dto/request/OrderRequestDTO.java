package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private Integer customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String notes;
    private List<OrderItemRequestDTO> orderItems;
}

