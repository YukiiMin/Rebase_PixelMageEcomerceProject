package com.example.PixelMageEcomerceProject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.PixelMageEcomerceProject.entity.Voucher;
import com.example.PixelMageEcomerceProject.repository.VoucherRepository;
import com.example.PixelMageEcomerceProject.service.impl.VoucherServiceImpl;

public class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateVoucher_Success() {
        Integer userId = 1;
        Voucher mockVoucher = new Voucher(1L, "TESTCODE", 10, 20000, userId, LocalDateTime.now(), LocalDateTime.now().plusDays(30), false);
        
        when(voucherRepository.save(any(Voucher.class))).thenReturn(mockVoucher);

        Voucher result = voucherService.createVoucher(userId);

        assertNotNull(result);
        assertEquals("TESTCODE", result.getCode());
        assertEquals(10, result.getDiscountPct());
        assertEquals(20000, result.getMaxDiscountVnd());
        verify(voucherRepository).save(any(Voucher.class));
    }

    @Test
    public void testRedeemVoucher_Success_UnderMax() {
        String code = "TESTCODE";
        Integer userId = 1;
        BigDecimal orderTotal = new BigDecimal("100000"); // 10% = 10000 < 20000
        
        Voucher voucher = new Voucher(1L, code, 10, 20000, userId, LocalDateTime.now(), LocalDateTime.now().plusDays(30), false);
        
        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        BigDecimal discount = voucherService.redeemVoucher(code, userId, orderTotal);

        assertEquals(new BigDecimal("10000.0"), discount);
        assertEquals(true, voucher.getIsUsed());
        verify(voucherRepository).save(voucher);
    }

    @Test
    public void testRedeemVoucher_Success_OverMax() {
        String code = "TESTCODE";
        Integer userId = 1;
        BigDecimal orderTotal = new BigDecimal("300000"); // 10% = 30000 > 20000 -> Max 20000
        
        Voucher voucher = new Voucher(1L, code, 10, 20000, userId, LocalDateTime.now(), LocalDateTime.now().plusDays(30), false);
        
        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        BigDecimal discount = voucherService.redeemVoucher(code, userId, orderTotal);

        assertEquals(new BigDecimal("20000"), discount);
    }

    @Test
    public void testValidateVoucher_Expired() {
        String code = "TESTCODE";
        Integer userId = 1;
        BigDecimal orderTotal = new BigDecimal("100000");
        
        Voucher voucher = new Voucher(1L, code, 10, 20000, userId, LocalDateTime.now(), LocalDateTime.now().minusDays(1), false);
        
        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            voucherService.validateVoucher(code, userId, orderTotal);
        });

        assertEquals("Voucher đã hết hạn", ex.getMessage());
    }

    @Test
    public void testValidateVoucher_Used() {
        String code = "TESTCODE";
        Integer userId = 1;
        BigDecimal orderTotal = new BigDecimal("100000");
        
        Voucher voucher = new Voucher(1L, code, 10, 20000, userId, LocalDateTime.now(), LocalDateTime.now().plusDays(30), true);
        
        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            voucherService.validateVoucher(code, userId, orderTotal);
        });

        assertEquals("Voucher đã được sử dụng", ex.getMessage());
    }

    @Test
    public void testValidateVoucher_WrongOwner() {
        String code = "TESTCODE";
        Integer userId = 1;
        Integer otherUserId = 2;
        BigDecimal orderTotal = new BigDecimal("100000");
        
        Voucher voucher = new Voucher(1L, code, 10, 20000, otherUserId, LocalDateTime.now(), LocalDateTime.now().plusDays(30), false);
        
        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            voucherService.validateVoucher(code, userId, orderTotal);
        });

        assertEquals("Voucher không hợp lệ hoặc không thuộc về bạn", ex.getMessage());
    }

    @Test
    public void testValidateVoucher_NotFound() {
        String code = "INVALID_CODE";
        Integer userId = 1;
        BigDecimal orderTotal = new BigDecimal("100000");
        
        when(voucherRepository.findByCode(code)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            voucherService.validateVoucher(code, userId, orderTotal);
        });

        assertEquals("Voucher không tồn tại", ex.getMessage());
    }
}
