package com.example.PixelMageEcomerceProject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.PixelMageEcomerceProject.entity.PmPointWallet;
import com.example.PixelMageEcomerceProject.repository.PmPointWalletRepository;
import com.example.PixelMageEcomerceProject.service.impl.PmPointWalletServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;

public class PmPointWalletServiceTest {

    @Mock
    private PmPointWalletRepository pmPointWalletRepository;

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private PmPointWalletServiceImpl pmPointWalletService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExchangeForVoucher_Success() {
        Integer userId = 1;
        PmPointWallet wallet = new PmPointWallet(1L, userId, 1200);
        
        when(pmPointWalletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        pmPointWalletService.exchangeForVoucher(userId);

        assertEquals(200, wallet.getBalance());
        verify(pmPointWalletRepository).save(wallet);
        verify(voucherService).createVoucher(userId);
    }

    @Test
    public void testExchangeForVoucher_InsufficientBalance() {
        Integer userId = 1;
        PmPointWallet wallet = new PmPointWallet(1L, userId, 900);
        
        when(pmPointWalletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            pmPointWalletService.exchangeForVoucher(userId);
        });

        assertEquals("Số dư không đủ để đổi Voucher", ex.getMessage());
        verify(pmPointWalletRepository, never()).save(any());
        verify(voucherService, never()).createVoucher(userId);
    }

    @Test
    public void testCredit_Success() {
        Integer userId = 1;
        PmPointWallet wallet = new PmPointWallet(1L, userId, 500);
        
        when(pmPointWalletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        pmPointWalletService.credit(userId, 50);

        assertEquals(550, wallet.getBalance());
        verify(pmPointWalletRepository).save(wallet);
    }
}
