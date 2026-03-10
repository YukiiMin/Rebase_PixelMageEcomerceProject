package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final PackRepository packRepository;

    @Override
    public OrderItem createOrderItem(OrderItemRequestDTO orderItemRequestDTO) {
        Order order = orderRepository.findById(orderItemRequestDTO.getOrderId())
                .orElseThrow(
                        () -> new RuntimeException("Order not found with id: " + orderItemRequestDTO.getOrderId()));

        Pack pack = packRepository.findById(orderItemRequestDTO.getPackId())
                .orElseThrow(() -> new RuntimeException("Pack not found with id: " + orderItemRequestDTO.getPackId()));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setPack(pack);
        orderItem.setQuantity(orderItemRequestDTO.getQuantity());
        orderItem.setUnitPrice(orderItemRequestDTO.getUnitPrice());
        orderItem.setSubtotal(orderItemRequestDTO.getSubtotal());
        orderItem.setCustomText(orderItemRequestDTO.getCustomText());

        return orderItemRepository.save(orderItem);
    }

    @Override
    public OrderItem updateOrderItem(Integer id, OrderItemRequestDTO orderItemRequestDTO) {
        Optional<OrderItem> existingOrderItem = orderItemRepository.findById(id);
        if (existingOrderItem.isPresent()) {
            OrderItem updatedOrderItem = existingOrderItem.get();

            if (orderItemRequestDTO.getOrderId() != null) {
                Order order = orderRepository.findById(orderItemRequestDTO.getOrderId())
                        .orElseThrow(() -> new RuntimeException(
                                "Order not found with id: " + orderItemRequestDTO.getOrderId()));
                updatedOrderItem.setOrder(order);
            }

            if (orderItemRequestDTO.getPackId() != null) {
                Pack pack = packRepository.findById(orderItemRequestDTO.getPackId())
                        .orElseThrow(() -> new RuntimeException(
                                "Pack not found with id: " + orderItemRequestDTO.getPackId()));
                updatedOrderItem.setPack(pack);
            }

            updatedOrderItem.setQuantity(orderItemRequestDTO.getQuantity());
            updatedOrderItem.setUnitPrice(orderItemRequestDTO.getUnitPrice());
            updatedOrderItem.setSubtotal(orderItemRequestDTO.getSubtotal());
            updatedOrderItem.setCustomText(orderItemRequestDTO.getCustomText());
            return orderItemRepository.save(updatedOrderItem);
        }
        throw new RuntimeException("OrderItem not found with id: " + id);
    }

    @Override
    public void deleteOrderItem(Integer id) {
        if (!orderItemRepository.existsById(id)) {
            throw new RuntimeException("OrderItem not found with id: " + id);
        }
        orderItemRepository.deleteById(id);
    }

    @Override
    public Optional<OrderItem> getOrderItemById(Integer id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        return orderItemRepository.findByOrderOrderId(orderId);
    }

    @Override
    public List<OrderItem> getOrderItemsByPackId(Integer packId) {
        return orderItemRepository.findByPackPackId(packId);
    }
}
