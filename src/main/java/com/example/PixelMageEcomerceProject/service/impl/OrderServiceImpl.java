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
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.enums.OrderStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.event.PaymentSuccessEvent;
import com.example.PixelMageEcomerceProject.mapper.OrderItemMapper;
import com.example.PixelMageEcomerceProject.mapper.OrderMapper;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.OrderService;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
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
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
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

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(status -> {
            Order order = orderMapper.toEntity(orderRequestDTO);
            order.setAccount(account);

            // Initialize default values for required fields
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

            // Build OrderItems — reference Product only, Pack is assigned AFTER payment
            if (orderRequestDTO.getOrderItems() != null) {
                List<OrderItem> items = new ArrayList<>();
                for (OrderItemRequestDTO itemDto : orderRequestDTO.getOrderItems()) {
                    OrderItem item = orderItemMapper.toEntity(itemDto);
                    item.setOrder(savedOrder);

                    // Resolve Product (what the customer ordered)
                    if (itemDto.getProductId() != null) {
                        Product product = productRepository.findById(itemDto.getProductId())
                                .orElseThrow(() -> {
                                    log.error("[ORDER] Product not found: productId={}", itemDto.getProductId());
                                    return new RuntimeException("Product not found: " + itemDto.getProductId());
                                });
                        item.setProduct(product);
                        log.debug("[ORDER] OrderItem linked to product: productId={}, name={}",
                                product.getProductId(), product.getName());
                    } else {
                        throw new RuntimeException("productId is required in order items");
                    }

                    // Pack is intentionally NOT set here — will be assigned in handlePaymentSuccess()
                    items.add(item);
                    orderItemRepository.save(item);
                }
                savedOrder.setOrderItems(items);
            }

            log.info("[ORDER] createOrder complete: orderId={}, totalAmount={}", savedOrder.getOrderId(),
                    savedOrder.getTotalAmount());
            return orderMapper.toOrderResponse(savedOrder);
        });
    }

    @Override
    public Map<String, Object> createOrderWithPayment(OrderRequestDTO orderRequestDTO, String currency) {
        OrderResponse createdOrder = createOrder(orderRequestDTO);

        InitPaymentResult paymentResult = paymentService
                .initiatePayment(
                        createdOrder.getOrderId(),
                        createdOrder.getTotalAmount(),
                        currency != null ? currency : "VND");

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

        // Release any packs that were already assigned (edge case: already fulfilled partially)
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> {
                if (item.getPack() != null && PackStatus.SOLD.equals(item.getPack().getStatus())) {
                    // Note: SOLD packs typically cannot be restocked, but mark for review
                    log.warn("[ORDER] Cancelled order {} had SOLD pack {}", id, item.getPack().getPackId());
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

    /**
     * Triggered when payment webhook confirms success.
     * THIS is the correct place to assign a physical Pack to each OrderItem.
     * Flow: Payment confirmed → find STOCKED Pack for the product → set SOLD → link to item.
     */
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("[EVENT] Handling PaymentSuccessEvent for Order ID: {}", event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + event.getOrderId()));

        if (!PaymentStatus.PENDING.equals(order.getPaymentStatus())) {
            log.warn("[EVENT] Order {} already processed, skipping. paymentStatus={}",
                    order.getOrderId(), order.getPaymentStatus());
            return;
        }

        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        order.setStatus(OrderStatus.PROCESSING);

        // Assign physical Packs now that payment is confirmed
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getPack() != null) {
                    // Pack already assigned (shouldn't happen, but safe guard)
                    log.warn("[EVENT] OrderItem {} already has pack {}", item.getOrderItemId(), item.getPack().getPackId());
                    continue;
                }
                if (item.getProduct() == null) {
                    log.error("[EVENT] OrderItem {} has no product, cannot assign pack", item.getOrderItemId());
                    continue;
                }

                // Find first available (STOCKED) Pack for this product's PackCategory — FIFO
                List<Pack> stockedPacks = new ArrayList<>();
                if (item.getProduct().getPackCategory() != null) {
                    stockedPacks = packRepository.findByPackCategoryPackCategoryIdAndStatus(
                            item.getProduct().getPackCategory().getPackCategoryId(), PackStatus.STOCKED);
                }

                if (!stockedPacks.isEmpty()) {
                    Pack pack = stockedPacks.get(0);
                    pack.setStatus(PackStatus.SOLD);  // Skip RESERVED — go straight to SOLD
                    packRepository.save(pack);
                    item.setPack(pack);
                    orderItemRepository.save(item);
                    log.info("[EVENT] Pack {} (product={}) assigned to OrderItem {} and marked SOLD",
                            pack.getPackId(), item.getProduct().getProductId(), item.getOrderItemId());
                } else {
                    log.warn("[EVENT] No STOCKED packs available for productId={}. OrderItem {} pending fulfillment.",
                            item.getProduct().getProductId(), item.getOrderItemId());
                    // Order still completes — pack fulfillment can be done manually by staff
                    // TODO: Trigger staff notification for manual pack assignment
                }
            }
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        log.info("[EVENT] Order {} is now COMPLETED", order.getOrderId());

        // Push real-time notification to user
        Integer userId = order.getAccount() != null ? order.getAccount().getCustomerId() : null;
        if (userId != null) {
            wsNotificationService.pushToUser(userId,
                    NotificationEvent.paymentConfirmed(userId, order.getOrderId(), event.getTransactionId()));
        }
        // Broadcast to admin
        wsNotificationService.pushToTopic("admin.notifications",
                NotificationEvent.orderPaid(order.getOrderId(), order.getTotalAmount()));
    }
}
