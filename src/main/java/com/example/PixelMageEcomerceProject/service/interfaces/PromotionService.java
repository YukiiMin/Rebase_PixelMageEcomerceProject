package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.PromotionRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Promotion;

public interface PromotionService {
    Promotion createPromotion (PromotionRequestDTO promotionRequest);
    Promotion setOrderPromotion (int orderId, int promotionId);
}
