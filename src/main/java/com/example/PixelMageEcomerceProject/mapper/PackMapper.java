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

    @Mapping(target = "packCategoryId",       source = "packCategory.packCategoryId")
    @Mapping(target = "packCategoryName",     source = "packCategory.name")
    @Mapping(target = "packCategoryImageUrl", source = "packCategory.imageUrl")
    @Mapping(target = "createdByAccountId",   source = "createdBy.customerId")
    @Mapping(target = "packDetails",          source = "packDetails")
    PackResponse toResponse(Pack pack);

    @Mapping(target = "cardId",         source = "card.cardId")
    @Mapping(target = "cardName",       expression = "java(detail.getCardTemplate() != null ? detail.getCardTemplate().getName() : (detail.getCard() != null && detail.getCard().getCardTemplate() != null ? detail.getCard().getCardTemplate().getName() : null))")
    @Mapping(target = "rarity",         expression = "java(detail.getCardTemplate() != null ? detail.getCardTemplate().getRarity().name() : (detail.getCard() != null && detail.getCard().getCardTemplate() != null ? detail.getCard().getCardTemplate().getRarity().name() : null))")
    @Mapping(target = "imagePath",      expression = "java(detail.getCardTemplate() != null ? detail.getCardTemplate().getImagePath() : (detail.getCard() != null && detail.getCard().getCardTemplate() != null ? detail.getCard().getCardTemplate().getImagePath() : null))")
    @Mapping(target = "positionIndex",  source = "positionIndex")
    @Mapping(target = "cardStatus",     expression = "java(detail.getCard() != null && detail.getCard().getStatus() != null ? detail.getCard().getStatus().name() : null)")
    @Mapping(target = "nfcUid",         expression = "java(detail.getCard() != null ? detail.getCard().getNfcUid() : null)")
    PackResponse.Detail toDetailResponse(PackDetail detail);

    List<PackResponse> toResponses(List<Pack> packs);
}
