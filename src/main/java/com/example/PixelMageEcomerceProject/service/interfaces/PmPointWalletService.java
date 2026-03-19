package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.response.PmPointWalletResponse;

public interface PmPointWalletService {
    void credit(Integer userId, Integer amount);
    PmPointWalletResponse getWalletBalance(Integer userId);
    void exchangeForVoucher(Integer userId);
}
