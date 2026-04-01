package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface OrderService {
    OrderResponse createOrder(OrderRequestDTO orderRequestDTO);

    /**
     * Create order with payment intent for Stripe payment processing.
     */
    Map<String, Object> createOrderWithPayment(OrderRequestDTO orderRequestDTO, String currency);

    OrderResponse updateOrder(Integer id, OrderRequestDTO orderRequestDTO);
    void deleteOrder(Integer id);
    OrderResponse cancelOrder(Integer id);
    OrderResponse getOrderById(Integer id);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersByCustomerId(Integer customerId);
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
}
