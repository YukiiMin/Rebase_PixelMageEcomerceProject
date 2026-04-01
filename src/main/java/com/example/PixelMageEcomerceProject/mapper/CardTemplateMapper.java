package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.CardTemplateResponse;
import com.example.PixelMageEcomerceProject.dto.response.CardTemplateSummaryResponse;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CardTemplateMapper {
    @Mapping(target = "frameworkName", source = "cardFramework.name")
    CardTemplateSummaryResponse toSummaryResponse(CardTemplate cardTemplate);

    @Mapping(target = "frameworkId", source = "cardFramework.frameworkId")
    @Mapping(target = "frameworkName", source = "cardFramework.name")
    CardTemplateResponse toResponse(CardTemplate cardTemplate);
}
