package com.example.PixelMageEcomerceProject.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderService;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.interfaces.RedisLockService;
import com.stripe.model.PaymentIntent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final PaymentService paymentService;
    private final PackRepository packRepository;
    private final OrderItemRepository orderItemRepository;
    private final RedisLockService redisLockService;

    @Override
    public Order createOrder(OrderRequestDTO orderRequestDTO) {
        Account account = accountRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(
                        () -> new RuntimeException("Account not found with id: " + orderRequestDTO.getCustomerId()));

        Order order = new Order();
        order.setAccount(account);
        order.setOrderDate(orderRequestDTO.getOrderDate());
        order.setStatus(orderRequestDTO.getStatus());
        order.setTotalAmount(orderRequestDTO.getTotalAmount());
        order.setShippingAddress(orderRequestDTO.getShippingAddress());
        order.setPaymentMethod(orderRequestDTO.getPaymentMethod());
        order.setPaymentStatus(orderRequestDTO.getPaymentStatus());
        order.setNotes(orderRequestDTO.getNotes());

        Order savedOrder = orderRepository.save(order);

        if (orderRequestDTO.getOrderItems() != null) {
            List<OrderItem> items = new ArrayList<>();
            for (OrderItemRequestDTO itemDto : orderRequestDTO.getOrderItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(savedOrder);
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setSubtotal(itemDto.getSubtotal());
                item.setCustomText(itemDto.getCustomText());

                if (itemDto.getPackId() != null) {
                    String lockKey = "pack:" + itemDto.getPackId() + ":lock";
                    if (!redisLockService.tryLock(lockKey, 5)) {
                        throw new RuntimeException(
                                "Pack " + itemDto.getPackId() + " is currently being reserved. Please try again.");
                    }
                    try {
                        Pack pack = packRepository.findById(itemDto.getPackId())
                                .orElseThrow(() -> new RuntimeException("Pack not found: " + itemDto.getPackId()));
                        if (!"STOCKED".equals(pack.getStatus())) {
                            throw new RuntimeException("Pack is not STOCKED anymore");
                        }
                        pack.setStatus("RESERVED");
                        packRepository.save(pack);
                        item.setPack(pack);
                    } finally {
                        redisLockService.releaseLock(lockKey);
                    }
                }

                items.add(item);
                orderItemRepository.save(item);
            }
            savedOrder.setOrderItems(items);
        }

        return savedOrder;
    }

    @Override
    public Map<String, Object> createOrderWithPayment(OrderRequestDTO orderRequestDTO, String currency) {
        // First create the order
        Order createdOrder = createOrder(orderRequestDTO);

        // Then create payment intent for the order
        PaymentIntent paymentIntent = paymentService.createPaymentIntent(
                createdOrder.getOrderId(),
                createdOrder.getTotalAmount(),
                currency != null ? currency : "usd");

        // Return combined response
        Map<String, Object> response = new HashMap<>();
        response.put("order", createdOrder);
        response.put("paymentIntent", Map.of(
                "id", paymentIntent.getId(),
                "clientSecret", paymentIntent.getClientSecret(),
                "status", paymentIntent.getStatus()));

        return response;
    }

    @Override
    public Order updateOrder(Integer id, OrderRequestDTO orderRequestDTO) {
        Optional<Order> existingOrder = orderRepository.findById(id);
        if (existingOrder.isPresent()) {
            Order updatedOrder = existingOrder.get();

            if (orderRequestDTO.getCustomerId() != null) {
                Account account = accountRepository.findById(orderRequestDTO.getCustomerId())
                        .orElseThrow(() -> new RuntimeException(
                                "Account not found with id: " + orderRequestDTO.getCustomerId()));
                updatedOrder.setAccount(account);
            }

            if (orderRequestDTO.getOrderDate() != null) {
                updatedOrder.setOrderDate(orderRequestDTO.getOrderDate());
            }
            if (orderRequestDTO.getStatus() != null) {
                updatedOrder.setStatus(orderRequestDTO.getStatus());
            }
            if (orderRequestDTO.getTotalAmount() != null) {
                updatedOrder.setTotalAmount(orderRequestDTO.getTotalAmount());
            }
            if (orderRequestDTO.getShippingAddress() != null) {
                updatedOrder.setShippingAddress(orderRequestDTO.getShippingAddress());
            }
            if (orderRequestDTO.getPaymentMethod() != null) {
                updatedOrder.setPaymentMethod(orderRequestDTO.getPaymentMethod());
            }
            if (orderRequestDTO.getPaymentStatus() != null) {
                updatedOrder.setPaymentStatus(orderRequestDTO.getPaymentStatus());
            }

            if ("PAID".equals(orderRequestDTO.getPaymentStatus()) && updatedOrder.getOrderItems() != null) {
                for (OrderItem item : updatedOrder.getOrderItems()) {
                    if (item.getPack() != null && "RESERVED".equals(item.getPack().getStatus())) {
                        Pack pack = item.getPack();
                        pack.setStatus("SOLD");
                        packRepository.save(pack);
                    }
                }
            }

            if (orderRequestDTO.getNotes() != null) {
                updatedOrder.setNotes(orderRequestDTO.getNotes());
            }
            return orderRepository.save(updatedOrder);
        }
        throw new RuntimeException("Order not found with id: " + id);
    }

    @Override
    public void deleteOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setIsActive(false);
        orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getPack() != null && "RESERVED".equals(item.getPack().getStatus())) {
                    Pack pack = item.getPack();
                    pack.setStatus("STOCKED");
                    packRepository.save(pack);
                }
            });
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByCustomerId(Integer customerId) {
        return orderRepository.findByAccountCustomerId(customerId);
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
}
