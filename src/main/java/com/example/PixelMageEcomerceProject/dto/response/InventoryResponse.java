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
public class InventoryResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private LocalDateTime lastChecked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
