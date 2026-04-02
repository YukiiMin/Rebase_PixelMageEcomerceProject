package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.CardTemplateResponse;

import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DivineHelperMapper.class, CardContentMapper.class})
public interface CardTemplateMapper {
    @Mapping(target = "frameworkName", source = "cardFramework.name")
    CardTemplateResponse.Summary toSummaryResponse(CardTemplate cardTemplate);

    @Mapping(target = "frameworkId", source = "cardFramework.frameworkId")
    @Mapping(target = "frameworkName", source = "cardFramework.name")
    @Mapping(target = "contents", source = "cardContents")
    CardTemplateResponse toResponse(CardTemplate cardTemplate);
}
