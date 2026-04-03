package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.PromotionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {
    List<PromotionResponse> getAllPromotions();
    PromotionResponse getPromotionById(int id);
    PromotionResponse createPromotion(PromotionRequestDTO promotionRequest);
    PromotionResponse updatePromotion(int id, PromotionRequestDTO promotionRequest);
    void deletePromotion(int id);
    PromotionResponse setOrderPromotion(int orderId, int promotionId);
}
