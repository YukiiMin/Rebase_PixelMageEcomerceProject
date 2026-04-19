package com.example.PixelMageEcomerceProject.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.PixelMageEcomerceProject.dto.response.ProductResponse;

import com.example.PixelMageEcomerceProject.entity.Product;

@Mapper(componentModel = "spring", uses = { CardTemplateMapper.class })
public interface ProductMapper {

    @Mapping(target = "poolSize", ignore = true)
    @Mapping(target = "stockCount", ignore = true)
    @Mapping(target = "poolPreview", ignore = true)
    @Mapping(target = "packCategoryId", source = "packCategory.packCategoryId")
    @Mapping(target = "cardTemplateId", source = "cardTemplate.cardTemplateId")
    ProductResponse toProductResponse(Product product);
}
