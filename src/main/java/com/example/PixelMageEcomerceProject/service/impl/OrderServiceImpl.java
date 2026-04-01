package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.example.PixelMageEcomerceProject.event.PaymentSuccessEvent;

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.mapper.OrderMapper;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.exceptions.PackReservationException;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderService;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.interfaces.RedisLockService;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final PaymentService paymentService;
    private final PackRepository packRepository;
    private final OrderItemRepository orderItemRepository;
    private final RedisLockService redisLockService;
    private final VoucherService voucherService;
    private final PlatformTransactionManager transactionManager;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse createOrder(OrderRequestDTO orderRequestDTO) {
        Account account = accountRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(
                        () -> new RuntimeException("Account not found with id: " + orderRequestDTO.getCustomerId()));

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        List<String> acquiredLocks = new ArrayList<>();
        try {
            // Pre-process items and acquire locks
            if (orderRequestDTO.getOrderItems() != null) {
                for (OrderItemRequestDTO itemDto : orderRequestDTO.getOrderItems()) {
                    if (itemDto.getPackId() != null) {
                        String lockKey = "lock:pack:" + itemDto.getPackId();
                        boolean locked;
                        try {
                            locked = redisLockService.tryLock(lockKey, 30); // Use a safe timeout
                        } catch (Exception e) {
                            log.error("[LOCK] Redis unavailable for pack {}: {}", itemDto.getPackId(), e.getMessage());
                            throw new RedisUnavailableException("Dịch vụ đặt hàng tạm thời không khả dụng. Vui lòng thử lại sau.");
                        }
                        if (!locked) {
                            throw new PackReservationException("Pack " + itemDto.getPackId() + " đang được người khác đặt. Vui lòng thử lại sau.");
                        }
                        acquiredLocks.add(lockKey);
                    }
                }
            }

            // Execute DB operations in a transaction
            return transactionTemplate.execute(status -> {
                Order order = new Order();
                order.setAccount(account);
                order.setOrderDate(orderRequestDTO.getOrderDate());
                order.setStatus(orderRequestDTO.getStatus());
                order.setShippingAddress(orderRequestDTO.getShippingAddress());
                order.setPaymentMethod(orderRequestDTO.getPaymentMethod());
                order.setPaymentStatus(orderRequestDTO.getPaymentStatus());
                order.setNotes(orderRequestDTO.getNotes());

                if (orderRequestDTO.getVoucherCode() != null && !orderRequestDTO.getVoucherCode().trim().isEmpty()) {
                    BigDecimal discount = voucherService.redeemVoucher(orderRequestDTO.getVoucherCode(),
                            orderRequestDTO.getCustomerId(), orderRequestDTO.getTotalAmount());
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
                            Pack pack = packRepository.findById(itemDto.getPackId())
                                    .orElseThrow(() -> new RuntimeException("Pack not found: " + itemDto.getPackId()));
                            if (!PackStatus.STOCKED.equals(pack.getStatus())) {
                                throw new RuntimeException("Pack " + itemDto.getPackId() + " is not STOCKED anymore");
                            }
                            pack.setStatus(PackStatus.RESERVED);
                            packRepository.save(pack);
                            item.setPack(pack);
                        }
                        items.add(item);
                        orderItemRepository.save(item);
                    }
                    savedOrder.setOrderItems(items);
                }
                return orderMapper.toOrderResponse(savedOrder);
            });

        } finally {
            // Ensure locks are released AFTER transaction commit/rollback
            for (String lockKey : acquiredLocks) {
                redisLockService.releaseLock(lockKey);
            }
        }
    }

    @Override
    public Map<String, Object> createOrderWithPayment(OrderRequestDTO orderRequestDTO, String currency) {
        // First create the order
        OrderResponse createdOrder = createOrder(orderRequestDTO);

        // Then initialize payment using the active gateway
        com.example.PixelMageEcomerceProject.service.model.InitPaymentResult paymentResult = paymentService
                .initiatePayment(
                        createdOrder.getOrderId(),
                        createdOrder.getTotalAmount(),
                        currency != null ? currency : "VND"); // Default to VND for SEPay

        // Return combined response
        Map<String, Object> response = new HashMap<>();
        response.put("order", createdOrder);
        response.put("payment", paymentResult);

        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Integer id, OrderRequestDTO orderRequestDTO) {
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

            if (PaymentStatus.SUCCEEDED.equals(orderRequestDTO.getPaymentStatus())
                    && updatedOrder.getOrderItems() != null) {
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
            return orderMapper.toOrderResponse(orderRepository.save(updatedOrder));
        }
        throw new RuntimeException("Order not found with id: " + id);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setIsActive(false);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Integer id) {
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
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrderById(Integer id) {
        return orderRepository.findById(id).map(orderMapper::toOrderResponse).orElse(null);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toOrderResponse).toList();
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(Integer customerId) {
        return orderRepository.findByAccountCustomerId(customerId).stream().map(orderMapper::toOrderResponse).toList();
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream().map(orderMapper::toOrderResponse).toList();
    }

    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("[EVENT] Handling PaymentSuccessEvent for Order ID: {}", event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + event.getOrderId()));

        if (PaymentStatus.PENDING.equals(order.getPaymentStatus())) {
            order.setPaymentStatus(PaymentStatus.SUCCEEDED);
            order.setStatus(OrderStatus.PROCESSING);

            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getPack() != null && PackStatus.RESERVED.equals(item.getPack().getStatus())) {
                        Pack pack = item.getPack();
                        pack.setStatus(PackStatus.SOLD);
                        packRepository.save(pack);
                        log.info("[EVENT] Updated Pack Status to SOLD: {}", pack.getPackId());
                    }
                }
            }
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("[EVENT] Order {} is now COMPLETED", order.getOrderId());
        }
    }
}
