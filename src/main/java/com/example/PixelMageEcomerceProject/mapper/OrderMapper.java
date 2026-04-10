package com.example.PixelMageEcomerceProject.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Voucher;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = { AccountMapper.class, ProductMapper.class, OrderItemMapper.class })
public abstract class OrderMapper {

    @Value("${sepay.bank-account}")
    private String bankAccount;

    @Value("${sepay.bank-code}")
    private String bankCode;

    @Mapping(target = "customer", source = "account")
    @Mapping(target = "appliedVoucher", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "paymentQrUrl", ignore = true)
    public abstract OrderResponse toOrderResponse(Order order);

    @AfterMapping
    protected void addPaymentQrAndVoucher(Order order, @MappingTarget OrderResponse response) {
        if (response.getTotalAmount() != null) {
            response.setFinalAmount(response.getTotalAmount());
            response.setDiscountAmount(BigDecimal.ZERO);
        }

        if ("VNPAY".equalsIgnoreCase(order.getPaymentMethod()) && PaymentStatus.PENDING.equals(order.getPaymentStatus())) {
            String description = "PIXELMAGE_ORD_" + order.getOrderId();
            String vietQrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact.png?amount=%s&addInfo=%s",
                bankCode,
                bankAccount,
                order.getTotalAmount() != null ? order.getTotalAmount().toBigInteger() : 0,
                description
            );
            response.setPaymentQrUrl(vietQrUrl);
        }
    }

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "account", ignore = true) // Set in service
    @Mapping(target = "orderItems", ignore = true) // Handle separately in service
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    public abstract Order toEntity(OrderRequestDTO dto);

    public abstract OrderResponse.AppliedVoucher toAppliedVoucherResponse(Voucher voucher);
}
