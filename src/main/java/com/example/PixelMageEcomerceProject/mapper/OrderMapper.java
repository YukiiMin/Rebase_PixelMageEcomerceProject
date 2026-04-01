package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.dto.response.OrderItemResponse;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {AccountMapper.class, ProductMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customer", source = "account")
    @Mapping(target = "appliedVoucher", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "paymentQrUrl", ignore = true)
    OrderResponse toOrderResponse(Order order);

    OrderResponse.AppliedVoucherResponse toAppliedVoucherResponse(Voucher voucher);

    @Mapping(target = "orderItemId", source = "orderItemId")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
