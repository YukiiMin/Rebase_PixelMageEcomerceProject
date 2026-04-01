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
public class VoucherResponse {
    private Integer voucherId;
    private String code;
    private Integer discountPct;
    private Integer maxDiscountVnd;
    private LocalDateTime expiresAt;
    private Boolean isUsed;
    private Boolean isExpired;
    private Integer daysUntilExpiry;
}
