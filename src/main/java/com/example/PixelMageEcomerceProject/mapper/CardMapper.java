package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.request.CardRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    @Mapping(target = "cardTemplate", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "nfcUid", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "owner", ignore = true)
    Card toEntity(CardRequestDTO dto);
}
