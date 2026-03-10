package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.entity.OrderItem;

@Service
public interface OrderItemService {
    OrderItem createOrderItem(OrderItemRequestDTO orderItemRequestDTO);

    OrderItem updateOrderItem(Integer id, OrderItemRequestDTO orderItemRequestDTO);

    void deleteOrderItem(Integer id);

    Optional<OrderItem> getOrderItemById(Integer id);

    List<OrderItem> getAllOrderItems();

    List<OrderItem> getOrderItemsByOrderId(Integer orderId);

    List<OrderItem> getOrderItemsByPackId(Integer packId);
}
