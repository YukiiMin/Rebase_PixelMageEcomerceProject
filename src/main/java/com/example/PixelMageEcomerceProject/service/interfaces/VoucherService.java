package com.example.PixelMageEcomerceProject.service.interfaces;

import java.math.BigDecimal;
import java.util.List;

import com.example.PixelMageEcomerceProject.dto.response.VoucherResponse;

public interface VoucherService {
    VoucherResponse createVoucher(Integer userId);
    BigDecimal redeemVoucher(String code, Integer userId, BigDecimal orderTotal);
    BigDecimal validateVoucher(String code, Integer userId, BigDecimal orderTotal);
    List<VoucherResponse> getMyVouchers(Integer userId);
}
