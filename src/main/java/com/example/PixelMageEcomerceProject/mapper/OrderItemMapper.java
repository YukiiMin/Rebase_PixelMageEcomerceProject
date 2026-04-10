package com.example.PixelMageEcomerceProject.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.Product;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "pack", source = "pack")
    @Mapping(target = "product", source = "product")
    OrderResponse.Item toResponse(OrderItem orderItem);

    @Mapping(target = "orderItemId", ignore = true)
    @Mapping(target = "order", ignore = true)     // Set in service
    @Mapping(target = "product", ignore = true)   // Looked up and set in service
    @Mapping(target = "pack", ignore = true)      // Assigned AFTER payment, not at order creation
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(OrderItemRequestDTO dto);

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
