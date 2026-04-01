package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.response.VoucherResponse;
import com.example.PixelMageEcomerceProject.entity.Voucher;
import com.example.PixelMageEcomerceProject.repository.VoucherRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;

import com.example.PixelMageEcomerceProject.mapper.VoucherMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public Voucher createVoucher(Integer userId) {
        Voucher voucher = new Voucher();
        voucher.setCode(UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8));
        voucher.setDiscountPct(10);
        voucher.setMaxDiscountVnd(20000);
        voucher.setOwnerId(userId);
        voucher.setExpiresAt(LocalDateTime.now().plusDays(30));
        voucher.setIsUsed(false);
        return voucherRepository.save(voucher);
    }

    @Override
    public BigDecimal redeemVoucher(String code, Integer userId, BigDecimal orderTotal) {
        BigDecimal discountValue = validateVoucherInternal(code, userId, orderTotal);
        
        Voucher voucher = voucherRepository.findByCode(code).get(); // present validated inside internal
        voucher.setIsUsed(true);
        voucherRepository.save(voucher);
        
        log.info("Redeemed voucher {} for user {} with discount {}", code, userId, discountValue);
        return discountValue;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal validateVoucher(String code, Integer userId, BigDecimal orderTotal) {
        return validateVoucherInternal(code, userId, orderTotal);
    }

    private BigDecimal validateVoucherInternal(String code, Integer userId, BigDecimal orderTotal) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));

        if (!voucher.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Voucher không hợp lệ hoặc không thuộc về bạn");
        }

        if (Boolean.TRUE.equals(voucher.getIsUsed())) {
            throw new IllegalArgumentException("Voucher đã được sử dụng");
        }

        if (voucher.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Voucher đã hết hạn");
        }

        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountPct = BigDecimal.valueOf(voucher.getDiscountPct()).divide(BigDecimal.valueOf(100));
        BigDecimal calculatedDiscount = orderTotal.multiply(discountPct);
        BigDecimal maxDiscount = BigDecimal.valueOf(voucher.getMaxDiscountVnd());
        
        if (calculatedDiscount.compareTo(maxDiscount) > 0) {
            return maxDiscount;
        }
        return calculatedDiscount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getMyVouchers(Integer userId) {
        List<Voucher> vouchers = voucherRepository.findByOwnerIdAndIsUsedFalseAndExpiresAtAfter(userId, LocalDateTime.now());
        return vouchers.stream()
                .map(voucherMapper::toVoucherResponse)
                .collect(Collectors.toList());
    }
}
