package com.example.PixelMageEcomerceProject.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateVoucherRequestDTO {
    private String code;
    private BigDecimal orderTotal;
}
