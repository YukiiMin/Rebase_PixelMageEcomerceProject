package com.example.PixelMageEcomerceProject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.response.PmPointWalletResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.service.interfaces.PmPointWalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final PmPointWalletService pmPointWalletService;

    @GetMapping("/balance")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseBase<PmPointWalletResponse>> getWalletBalance(Authentication auth) {
        Integer userId = extractUserId(auth);
        return ResponseBase.ok(pmPointWalletService.getWalletBalance(userId), "Wallet balance retrieved successfully");
    }

    @PostMapping("/exchange")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseBase<Void>> exchangeForVoucher(Authentication auth) {
        Integer userId = extractUserId(auth);
        pmPointWalletService.exchangeForVoucher(userId);
        return ResponseBase.success("Exchanged successfully");
    }

    private Integer extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Account account) {
            return account.getCustomerId();
        }
        throw new RuntimeException("Could not extract userId from authentication context");
    }
}
