package com.example.PixelMageEcomerceProject.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private String code;
    private Integer discountPct;
    private Integer maxDiscountVnd;
    private LocalDateTime expiresAt;
    private Boolean isUsed;
}
