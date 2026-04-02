package com.example.PixelMageEcomerceProject.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.PixelMageEcomerceProject.dto.response.ProductResponse;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.enums.PackStatus;

@Mapper(componentModel = "spring", uses = { CardTemplateMapper.class })
public interface ProductMapper {

    @Mapping(target = "poolSize", source = "product", qualifiedByName = "calculatePoolSize")
    @Mapping(target = "stockCount", source = "packs", qualifiedByName = "calculateStockCount")
    @Mapping(target = "poolPreview", source = "cardPools")
    ProductResponse toProductResponse(Product product);

    @Named("calculatePoolSize")
    default Integer calculatePoolSize(Product product) {
        return (product.getCardPools() != null) ? product.getCardPools().size() : 0;
    }

    @Named("calculateStockCount")
    default Integer calculateStockCount(List<Pack> packs) {
        if (packs == null)
            return 0;
        return (int) packs.stream()
                .filter(p -> p.getStatus() == PackStatus.STOCKED)
                .count();
    }
}
