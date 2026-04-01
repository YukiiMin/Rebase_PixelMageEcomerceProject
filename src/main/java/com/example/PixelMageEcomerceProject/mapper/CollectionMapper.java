package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.CollectionItemResponse;
import com.example.PixelMageEcomerceProject.dto.response.CollectionResponse;
import com.example.PixelMageEcomerceProject.entity.CardCollection;
import com.example.PixelMageEcomerceProject.entity.CollectionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CollectionMapper {

    @Mapping(target = "collectionId", source = "collectionId")
    @Mapping(target = "collectionName", source = "collectionName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "collectionType", source = "collectionType")
    @Mapping(target = "isPublic", source = "isPublic")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "rewardType", source = "rewardType")
    @Mapping(target = "ownedCount", ignore = true)
    @Mapping(target = "requiredCount", ignore = true)
    @Mapping(target = "completionPercent", ignore = true)
    @Mapping(target = "isCompleted", ignore = true)
    @Mapping(target = "items", source = "collectionItems")
    CollectionResponse toCollectionResponse(CardCollection collection);

    @Mapping(target = "collectionItemId", source = "collectionItemId")
    @Mapping(target = "cardTemplateId", source = "cardTemplate.cardTemplateId")
    @Mapping(target = "name", source = "cardTemplate.name")
    @Mapping(target = "imagePath", source = "cardTemplate.imagePath")
    @Mapping(target = "rarity", source = "cardTemplate.rarity")
    @Mapping(target = "owned", constant = "false")
    CollectionItemResponse toCollectionItemResponse(CollectionItem item);
}
