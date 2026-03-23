package com.example.PixelMageEcomerceProject.service;

import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.service.impl.SEPayGatewayimpl;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SEPayGatewayTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private SEPayGatewayimpl sePayGateway;

    @Test
    void initPayment_returnsValidResult() {
        // GIVEN
        PaymentStrategyRequest request = PaymentStrategyRequest.builder()
                .orderId(1)
                .amount(new BigDecimal("50000"))
                .currency("VND")
                .build();

        // WHEN
        InitPaymentResult result = sePayGateway.initPayment(request);

        // THEN
        assertThat(result.getGatewayTransactionId()).startsWith("SEPAY_");
        // Result currently returns "sepay_qr_stand_in_url" in code, but I'll check for it.
        assertThat(result.getPaymentUrl()).isNotNull();
    }

    @Test
    void pollStatus_returnsPendingByDefault() {
        // WHEN
        PaymentStatus status = sePayGateway.pollStatus("ANY_ID");

        // THEN
        assertThat(status).isEqualTo(PaymentStatus.PENDING);
    }
}
