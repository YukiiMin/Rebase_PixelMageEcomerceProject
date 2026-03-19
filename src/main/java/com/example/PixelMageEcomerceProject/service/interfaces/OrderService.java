package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface OrderService {
    Order createOrder(OrderRequestDTO orderRequestDTO);

    /**
     * Create order with payment intent for Stripe payment processing.
     */
    Map<String, Object> createOrderWithPayment(OrderRequestDTO orderRequestDTO, String currency);

    Order updateOrder(Integer id, OrderRequestDTO orderRequestDTO);
    void deleteOrder(Integer id);
    Order cancelOrder(Integer id);
    Optional<Order> getOrderById(Integer id);
    List<Order> getAllOrders();
    List<Order> getOrdersByCustomerId(Integer customerId);
    List<Order> getOrdersByStatus(OrderStatus status);
}
