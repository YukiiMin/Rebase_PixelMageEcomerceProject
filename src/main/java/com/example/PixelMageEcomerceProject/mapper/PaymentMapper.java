package com.example.PixelMageEcomerceProject.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.PixelMageEcomerceProject.dto.response.PaymentResponseDTO;
import com.example.PixelMageEcomerceProject.entity.Payment;

@Mapper
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.orderId")
    PaymentResponseDTO toPaymentResponseDTO(Payment payment);

}
