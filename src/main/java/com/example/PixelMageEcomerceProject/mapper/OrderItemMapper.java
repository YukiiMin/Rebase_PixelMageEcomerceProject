package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "pack", source = "pack")
    OrderResponse.Item toResponse(OrderItem orderItem);

    @Mapping(target = "packId", source = "packId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "product", source = "product")
    OrderResponse.PackSummary packToSummary(Pack pack);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "imageUrl", source = "imageUrl")
    OrderResponse.ProductSummary productToSummary(Product product);

    List<OrderResponse.Item> toResponses(List<OrderItem> orderItems);
}
