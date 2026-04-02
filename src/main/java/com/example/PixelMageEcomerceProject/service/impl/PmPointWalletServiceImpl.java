package com.example.PixelMageEcomerceProject.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.response.PmPointWalletResponse;
import com.example.PixelMageEcomerceProject.entity.PmPointWallet;
import com.example.PixelMageEcomerceProject.repository.PmPointWalletRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.PmPointWalletService;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;

import com.example.PixelMageEcomerceProject.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PmPointWalletServiceImpl implements PmPointWalletService {

    private final PmPointWalletRepository pmPointWalletRepository;
    private final VoucherService voucherService;
    private final WalletMapper walletMapper;

    @Override
    public void credit(Integer userId, Integer amount) {
        if (amount == null || amount <= 0) {
            return;
        }
        Optional<PmPointWallet> walletOpt = pmPointWalletRepository.findByUserId(userId);
        PmPointWallet wallet;
        if (walletOpt.isEmpty()) {
            wallet = new PmPointWallet();
            wallet.setUserId(userId);
            wallet.setBalance(amount);
        } else {
            wallet = walletOpt.get();
            wallet.setBalance(wallet.getBalance() + amount);
        }
        pmPointWalletRepository.save(wallet);
        log.info("Credited {} PM_point. New balance is {}", amount, wallet.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public PmPointWalletResponse getWalletBalance(Integer userId) {
        PmPointWallet wallet = pmPointWalletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    PmPointWallet newWallet = new PmPointWallet();
                    newWallet.setUserId(userId);
                    newWallet.setBalance(0);
                    return pmPointWalletRepository.save(newWallet);
                });
        return walletMapper.toPmPointWalletResponse(wallet);
    }

    @Override
    @Transactional
    public void exchangeForVoucher(Integer userId) {
        PmPointWallet wallet = pmPointWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ví PM_point"));
        
        if (wallet.getBalance() < 1000) {
            throw new IllegalArgumentException("Số dư không đủ để đổi Voucher");
        }
        
        wallet.setBalance(wallet.getBalance() - 1000);
        pmPointWalletRepository.save(wallet);
        
        voucherService.createVoucher(userId);
        log.info("Exchanged 1000 PM_point for 1 Voucher for user {}", userId);
    }
}
