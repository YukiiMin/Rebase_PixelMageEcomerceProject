package com.example.PixelMageEcomerceProject.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.ValidateVoucherRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.dto.response.VoucherResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseBase<List<VoucherResponse>>> getMyVouchers(Authentication auth) {
        Integer userId = extractUserId(auth);
        return ResponseBase.ok(voucherService.getMyVouchers(userId), "Vouchers retrieved successfully");
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseBase<BigDecimal>> validateVoucher(@RequestBody ValidateVoucherRequestDTO request, Authentication auth) {
        Integer userId = extractUserId(auth);
        BigDecimal discount = voucherService.validateVoucher(request.getCode(), userId, request.getOrderTotal());
        return ResponseBase.ok(discount, "Voucher validated successfully");
    }

    private Integer extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Account account) {
            return account.getCustomerId();
        }
        throw new RuntimeException("Could not extract userId from authentication context");
    }
}
