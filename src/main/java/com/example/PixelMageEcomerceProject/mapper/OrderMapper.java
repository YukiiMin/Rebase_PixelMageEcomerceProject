package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AccountMapper.class, ProductMapper.class, OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customer", source = "account")
    @Mapping(target = "appliedVoucher", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "paymentQrUrl", ignore = true)
    OrderResponse toOrderResponse(Order order);

    OrderResponse.AppliedVoucher toAppliedVoucherResponse(Voucher voucher);

    // delegated to OrderItemMapper
    // OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
