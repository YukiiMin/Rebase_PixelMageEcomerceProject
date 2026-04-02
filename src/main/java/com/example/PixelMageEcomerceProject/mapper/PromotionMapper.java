package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.PromotionResponse;
import com.example.PixelMageEcomerceProject.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {
    PromotionResponse toResponse(Promotion promotion);
    List<PromotionResponse> toResponses(List<Promotion> promotions);
}
