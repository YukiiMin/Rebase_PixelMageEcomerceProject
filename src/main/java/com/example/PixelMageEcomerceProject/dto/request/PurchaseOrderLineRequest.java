package com.example.PixelMageEcomerceProject.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderLineRequest {
    private Integer productId;
    private int quantityOrdered;
    private int quantityReceived;
    private int quantityPendingReceived;
    private double unitPrice;
    private double totalPrice;
    private LocalDate expectedDate;
    private String note;
}
