package com.example.PixelMageEcomerceProject.service;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentGateway;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.mapper.PaymentMapper;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.PaymentRepository;
import com.example.PixelMageEcomerceProject.service.impl.PaymentServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentGatewayStrategy;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;
import com.example.PixelMageEcomerceProject.service.model.PaymentStrategyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PackRepository packRepository;
    @Mock
    private WebSocketNotificationService wsNotificationService;
    @Mock
    private PaymentGatewayStrategy activeGateway;
    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderId(1);
        testOrder.setTotalAmount(new BigDecimal("100000"));
        Account account = new Account();
        account.setCustomerId(100);
        testOrder.setAccount(account);
    }

    @Test
    void initiatePayment_success_delegatesToStrategy() {
        // GIVEN
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        when(activeGateway.getGatewayType()).thenReturn(PaymentGateway.SEPAY);
        InitPaymentResult mockResult = InitPaymentResult.builder()
                .gatewayTransactionId("TXN_123")
                .paymentUrl("http://pay.me")
                .build();
        when(activeGateway.initPayment(any(PaymentStrategyRequest.class))).thenReturn(mockResult);

        // WHEN
        InitPaymentResult result = paymentService.initiatePayment(1, new BigDecimal("100000"), "VND");

        // THEN
        assertThat(result.getGatewayTransactionId()).isEqualTo("TXN_123");
        verify(activeGateway).initPayment(any(PaymentStrategyRequest.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void pollStatus_delegatesToStrategy() {
        // GIVEN
        when(activeGateway.pollStatus("TXN_123")).thenReturn(PaymentStatus.SUCCEEDED);

        // WHEN
        PaymentStatus status = paymentService.pollPaymentStatus("TXN_123");

        // THEN
        assertThat(status).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(activeGateway).pollStatus("TXN_123");
    }
}
