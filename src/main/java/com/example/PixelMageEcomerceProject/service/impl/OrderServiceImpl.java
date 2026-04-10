package com.example.PixelMageEcomerceProject.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;
import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.event.PaymentSuccessEvent;
import com.example.PixelMageEcomerceProject.exceptions.PackReservationException;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.mapper.OrderItemMapper;
import com.example.PixelMageEcomerceProject.mapper.OrderMapper;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderService;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.interfaces.RedisLockService;
import com.example.PixelMageEcomerceProject.service.interfaces.VoucherService;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import com.example.PixelMageEcomerceProject.service.model.InitPaymentResult;

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
    private final OrderItemMapper orderItemMapper;
    private final WebSocketNotificationService wsNotificationService;

    @Override
    public OrderResponse createOrder(OrderRequestDTO orderRequestDTO) {
        log.info("[ORDER] createOrder start: customerId={}, itemCount={}, totalAmount={}, voucherCode={}",
                orderRequestDTO.getCustomerId(),
                orderRequestDTO.getOrderItems() != null ? orderRequestDTO.getOrderItems().size() : 0,
                orderRequestDTO.getTotalAmount(),
                orderRequestDTO.getVoucherCode());

        Account account = accountRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> {
                    log.error("[ORDER] Account not found: customerId={}", orderRequestDTO.getCustomerId());
                    return new RuntimeException("Account not found with id: " + orderRequestDTO.getCustomerId());
                });
        log.debug("[ORDER] Account resolved: email={}, role={}", account.getEmail(),
                account.getRole() != null ? account.getRole().getRoleName() : "null");

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
                            locked = redisLockService.tryLock(lockKey, 30);
                        } catch (Exception e) {
                            log.error("[ORDER][LOCK] Redis unavailable for pack {}: {}", itemDto.getPackId(),
                                    e.getMessage());
                            throw new RedisUnavailableException(
                                    "Dịch vụ đặt hàng tạm thời không khả dụng. Vui lòng thử lại sau.");
                        }
                        if (!locked) {
                            log.warn("[ORDER][LOCK] Pack {} is already locked by another request", itemDto.getPackId());
                            throw new PackReservationException(
                                    "Pack " + itemDto.getPackId() + " đang được người khác đặt. Vui lòng thử lại sau.");
                        }
                        log.debug("[ORDER][LOCK] Lock acquired for pack {}", itemDto.getPackId());
                        acquiredLocks.add(lockKey);
                    }
                }
            }
// 
            // Execute DB operations in a transaction
            return transactionTemplate.execute(status -> {
                // Use MapStruct to map DTO to Entity
                Order order = orderMapper.toEntity(orderRequestDTO);
                order.setAccount(account);

                // Initialize default values for missing required fields
                if (order.getStatus() == null) {
                    order.setStatus(OrderStatus.PENDING);
                }
                if (order.getPaymentStatus() == null) {
                    order.setPaymentStatus(PaymentStatus.PENDING);
                }
                if (order.getOrderDate() == null) {
                    order.setOrderDate(java.time.LocalDateTime.now());
                }

                // Apply voucher if present
                if (orderRequestDTO.getVoucherCode() != null && !orderRequestDTO.getVoucherCode().trim().isEmpty()) {
                    log.debug("[ORDER] Applying voucher: code={}", orderRequestDTO.getVoucherCode());
                    BigDecimal discount = voucherService.redeemVoucher(orderRequestDTO.getVoucherCode(),
                            orderRequestDTO.getCustomerId(), orderRequestDTO.getTotalAmount());
                    BigDecimal newTotal = orderRequestDTO.getTotalAmount().subtract(discount);
                    if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
                        newTotal = BigDecimal.ZERO;
                    }
                    log.info("[ORDER] Voucher applied: discount={}, newTotal={}", discount, newTotal);
                    order.setTotalAmount(newTotal);
                }

                Order savedOrder = orderRepository.save(order);
                log.info("[ORDER] Order saved: orderId={}", savedOrder.getOrderId());

                if (orderRequestDTO.getOrderItems() != null) {
                    List<OrderItem> items = new ArrayList<>();
                    for (OrderItemRequestDTO itemDto : orderRequestDTO.getOrderItems()) {
                        // Use MapStruct to map DTO to Entity
                        OrderItem item = orderItemMapper.toEntity(itemDto);
                        item.setOrder(savedOrder);

                        if (itemDto.getPackId() != null) {
                            Pack pack = packRepository.findById(itemDto.getPackId())
                                    .orElseThrow(() -> {
                                        log.error("[ORDER] Pack not found: packId={}", itemDto.getPackId());
                                        return new RuntimeException("Pack not found: " + itemDto.getPackId());
                                    });
                            log.debug("[ORDER] Pack {} status = {}", pack.getPackId(), pack.getStatus());
                            if (!PackStatus.STOCKED.equals(pack.getStatus())) {
                                log.warn("[ORDER] Pack {} is not STOCKED, current status={}", pack.getPackId(),
                                        pack.getStatus());
                                throw new RuntimeException("Pack " + itemDto.getPackId() + " is not STOCKED anymore");
                            }
                            pack.setStatus(PackStatus.RESERVED);
                            packRepository.save(pack);
                            item.setPack(pack);
                            log.debug("[ORDER] Pack {} reserved", pack.getPackId());
                        }
                        items.add(item);
                        orderItemRepository.save(item);
                    }
                    savedOrder.setOrderItems(items);
                }

                log.info("[ORDER] createOrder complete: orderId={}, totalAmount={}", savedOrder.getOrderId(),
                        savedOrder.getTotalAmount());
                return orderMapper.toOrderResponse(savedOrder);
            });

        } finally {
            // Ensure locks are released AFTER transaction commit/rollback
            for (String lockKey : acquiredLocks) {
                redisLockService.releaseLock(lockKey);
                log.debug("[ORDER][LOCK] Released lock: {}", lockKey);
            }
        }
    }

    @Override
    public Map<String, Object> createOrderWithPayment(OrderRequestDTO orderRequestDTO, String currency) {
        // First create the order
        OrderResponse createdOrder = createOrder(orderRequestDTO);

        // Then initialize payment using the active gateway
        InitPaymentResult paymentResult = paymentService
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

            // Push real-time notification đến user
            Integer userId = order.getAccount() != null ? order.getAccount().getCustomerId() : null;
            if (userId != null) {
                wsNotificationService.pushToUser(userId,
                        NotificationEvent.paymentConfirmed(userId, order.getOrderId(), event.getTransactionId()));
            }
            // Broadcast đến admin notifications (/topic/admin.notifications)
            wsNotificationService.pushToTopic("admin.notifications",
                    NotificationEvent.orderPaid(order.getOrderId(), order.getTotalAmount()));
        }
    }
}
