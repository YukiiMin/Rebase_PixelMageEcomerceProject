package com.example.PixelMageEcomerceProject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.PixelMageEcomerceProject.config.VNPayConfig;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.Payment;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.event.PaymentSuccessEvent;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PaymentRepository;
import com.example.PixelMageEcomerceProject.service.impl.VNPayGatewayImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    @Mock
    private VNPayConfig vnPayConfig;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private HttpServletRequest request;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VNPayGatewayImpl vnPayService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void processIpn_success_firstTime() {
        // Mocking VNPay parameters
        List<String> paramNames = List.of(
                "vnp_TxnRef", "vnp_TransactionNo", "vnp_ResponseCode",
                "vnp_Amount", "vnp_SecureHash");
        when(request.getParameterNames()).thenReturn(
                Collections.enumeration(paramNames));
        when(request.getParameter("vnp_TxnRef")).thenReturn("123-random");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("TRANS123");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_Amount")).thenReturn("100000");
        when(request.getParameter("vnp_SecureHash")).thenReturn("VALID_HASH");

        // Mocking Service dependencies
        Order order = new Order();
        order.setOrderId(123);
        order.setPaymentStatus(PaymentStatus.PENDING);
        when(orderRepository.findById(123)).thenReturn(Optional.of(order));
        when(vnPayConfig.getVnp_HashSecret()).thenReturn("SECRET");

        // Mocking Redis for the first processing
        when(valueOperations.setIfAbsent(eq("payment:vnpay:TRANS123"), eq("processed"), any(Duration.class)))
                .thenReturn(true);

        try (MockedStatic<VNPayConfig> mockedConfig = mockStatic(VNPayConfig.class)) {
            mockedConfig.when(() -> VNPayConfig.hashAllFields(any(), eq("SECRET"))).thenReturn("VALID_HASH");

            Map<String, String> result = vnPayService.processIpn(request);

            assertEquals("00", result.get("RspCode"));
            assertEquals("Confirm Success", result.get("Message"));
            verify(paymentRepository).save(any(Payment.class));
            verify(eventPublisher).publishEvent(any(PaymentSuccessEvent.class));
            // order status update is now handled by event listener, so we don't verify
            // orderRepository.save here
        }
    }

    @Test
    void processIpn_duplicate_returnsSuccessImmediately() {
        // Mocking VNPay parameters
        List<String> paramNames = List.of(
                "vnp_TxnRef", "vnp_TransactionNo", "vnp_ResponseCode",
                "vnp_Amount", "vnp_SecureHash");
        when(request.getParameterNames()).thenReturn(
                Collections.enumeration(paramNames));
        when(request.getParameter("vnp_TxnRef")).thenReturn("123-random");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("TRANS123");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_Amount")).thenReturn("100000");
        when(request.getParameter("vnp_SecureHash")).thenReturn("VALID_HASH");

        // Mocking Service dependencies
        when(vnPayConfig.getVnp_HashSecret()).thenReturn("SECRET");

        // Mocking Redis: key already exists
        when(valueOperations.setIfAbsent(eq("payment:vnpay:TRANS123"), eq("processed"), any(Duration.class)))
                .thenReturn(false);

        try (MockedStatic<VNPayConfig> mockedConfig = mockStatic(VNPayConfig.class)) {
            mockedConfig.when(() -> VNPayConfig.hashAllFields(any(), eq("SECRET"))).thenReturn("VALID_HASH");

            Map<String, String> result = vnPayService.processIpn(request);

            assertEquals("00", result.get("RspCode"));
            assertEquals("Confirm Success", result.get("Message"));
            verify(orderRepository, never()).findById(any()); // Should skip lookup for duplicate
            verify(paymentRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Test
    void processIpn_redisUnavailable_throwsException() {
        // Mocking VNPay parameters
        List<String> paramNames = List.of(
                "vnp_TxnRef", "vnp_TransactionNo", "vnp_ResponseCode",
                "vnp_Amount", "vnp_SecureHash");
        when(request.getParameterNames()).thenReturn(
                Collections.enumeration(paramNames));
        when(request.getParameter("vnp_TxnRef")).thenReturn("123-random");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("TRANS123");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_Amount")).thenReturn("100000");
        when(request.getParameter("vnp_SecureHash")).thenReturn("VALID_HASH");

        // Mocking Service dependencies
        when(vnPayConfig.getVnp_HashSecret()).thenReturn("SECRET");

        // Mocking Redis failure
        when(valueOperations.setIfAbsent(anyString(), any(), any(Duration.class)))
                .thenThrow(new RuntimeException("Redis connection failed"));

        try (MockedStatic<VNPayConfig> mockedConfig = mockStatic(VNPayConfig.class)) {
            mockedConfig.when(() -> VNPayConfig.hashAllFields(any(), eq("SECRET"))).thenReturn("VALID_HASH");

            assertThrows(RedisUnavailableException.class, () -> vnPayService.processIpn(request));
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
