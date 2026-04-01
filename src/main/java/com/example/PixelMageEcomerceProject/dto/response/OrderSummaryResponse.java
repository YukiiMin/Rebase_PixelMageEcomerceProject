package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private Integer orderId;
    private LocalDateTime orderDate;
    private String status;
    private String paymentStatus;
    private Double finalAmount;
    private Integer itemCount;
}
