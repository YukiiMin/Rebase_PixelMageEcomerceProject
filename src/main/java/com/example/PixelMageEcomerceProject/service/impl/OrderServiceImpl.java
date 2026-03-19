package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
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
import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderService;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.exceptions.PackReservationException;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.service.interfaces.RedisLockService;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;
import lombok.extern.slf4j.Slf4j;
import com.stripe.model.PaymentIntent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final PaymentService paymentService;
    private final PackRepository packRepository;
    private final OrderItemRepository orderItemRepository;
    private final RedisLockService redisLockService;
    private final VoucherService voucherService;

    @Override
    public Order createOrder(OrderRequestDTO orderRequestDTO) {
        Account account = accountRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(
                        () -> new RuntimeException("Account not found with id: " + orderRequestDTO.getCustomerId()));

        Order order = new Order();
        order.setAccount(account);
        order.setOrderDate(orderRequestDTO.getOrderDate());
        order.setStatus(orderRequestDTO.getStatus());
        order.setShippingAddress(orderRequestDTO.getShippingAddress());
        order.setPaymentMethod(orderRequestDTO.getPaymentMethod());
        order.setPaymentStatus(orderRequestDTO.getPaymentStatus());
        order.setNotes(orderRequestDTO.getNotes());

        if (orderRequestDTO.getVoucherCode() != null && !orderRequestDTO.getVoucherCode().trim().isEmpty()) {
            BigDecimal discount = voucherService.redeemVoucher(orderRequestDTO.getVoucherCode(), orderRequestDTO.getCustomerId(), orderRequestDTO.getTotalAmount());
            BigDecimal newTotal = orderRequestDTO.getTotalAmount().subtract(discount);
            if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
                newTotal = BigDecimal.ZERO;
            }
            order.setTotalAmount(newTotal);
        } else {
            order.setTotalAmount(orderRequestDTO.getTotalAmount());
        }

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
                    String lockKey = "lock:pack:" + itemDto.getPackId();
                    // Fail-closed: Redis unavailable → 503. Double-booking is worse than downtime.
                    boolean locked;
                    try {
                        locked = redisLockService.tryLock(lockKey, 5);
                    } catch (Exception e) {
                        log.error("[LOCK] Redis unavailable for pack {}: {}", itemDto.getPackId(), e.getMessage());
                        throw new RedisUnavailableException(
                                "Dịch vụ đặt hàng tạm thời không khả dụng. Vui lòng thử lại sau.");
                    }
                    if (!locked) {
                        throw new PackReservationException(
                                "Pack đang được đặt. Thử lại sau vài giây.");
                    }
                    try {
                        Pack pack = packRepository.findById(itemDto.getPackId())
                                .orElseThrow(() -> new RuntimeException("Pack not found: " + itemDto.getPackId()));
                        if (!PackStatus.STOCKED.equals(pack.getStatus())) {
                            throw new RuntimeException("Pack is not STOCKED anymore");
                        }
                        pack.setStatus(PackStatus.RESERVED);
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

            if (PaymentStatus.SUCCEEDED.equals(orderRequestDTO.getPaymentStatus()) && updatedOrder.getOrderItems() != null) {
                for (OrderItem item : updatedOrder.getOrderItems()) {
                    if (item.getPack() != null && PackStatus.RESERVED.equals(item.getPack().getStatus())) {
                        Pack pack = item.getPack();
                        pack.setStatus(PackStatus.SOLD);
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
                if (item.getPack() != null && PackStatus.RESERVED.equals(item.getPack().getStatus())) {
                    Pack pack = item.getPack();
                    pack.setStatus(PackStatus.STOCKED);
                    packRepository.save(pack);
                }
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
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
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
}
