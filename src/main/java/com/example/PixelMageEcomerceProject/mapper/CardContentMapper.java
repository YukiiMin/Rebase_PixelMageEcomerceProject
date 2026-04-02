package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.CardContentResponse;
import com.example.PixelMageEcomerceProject.entity.CardContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardContentMapper {

    @Mapping(source = "cardTemplate.cardTemplateId", target = "cardTemplateId")
    @Mapping(source = "cardTemplate.name", target = "cardTemplateName")
    CardContentResponse toResponse(CardContent cardContent);

    List<CardContentResponse> toResponses(List<CardContent> cardContents);
}
