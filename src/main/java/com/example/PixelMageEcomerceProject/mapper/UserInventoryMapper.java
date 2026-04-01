package com.example.PixelMageEcomerceProject.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.PixelMageEcomerceProject.dto.response.UserInventoryResponse;
import com.example.PixelMageEcomerceProject.entity.UserInventory;

@Mapper(componentModel = "spring")
public interface UserInventoryMapper {

    @Mapping(target = "cardTemplateId", source = "cardTemplate.cardTemplateId")
    @Mapping(target = "name", source = "cardTemplate.name")
    @Mapping(target = "arcanaType", source = "cardTemplate.arcanaType")
    @Mapping(target = "rarity", source = "cardTemplate.rarity")
    @Mapping(target = "imagePath", source = "cardTemplate.imagePath")
    @Mapping(target = "linkedCardCount", source = "quantity")
    UserInventoryResponse toUserInventoryResponse(UserInventory userInventory);

}
