package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.PackResponse;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.PackDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PackMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "createdByAccountId", source = "createdBy.customerId")
    @Mapping(target = "packDetails", source = "packDetails")
    PackResponse toResponse(Pack pack);

    @Mapping(target = "cardId", source = "card.cardId")
    @Mapping(target = "cardName", source = "card.cardTemplate.name")
    PackResponse.Detail toDetailResponse(PackDetail detail);

    List<PackResponse> toResponses(List<Pack> packs);
}
