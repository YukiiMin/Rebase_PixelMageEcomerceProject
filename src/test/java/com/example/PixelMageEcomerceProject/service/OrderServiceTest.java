package com.example.PixelMageEcomerceProject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.mapper.OrderMapper;
import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.impl.OrderServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        SimpleTransactionStatus txStatus = new SimpleTransactionStatus(true);
        when(transactionManager.getTransaction(any())).thenReturn(txStatus);
        doNothing().when(transactionManager).commit(any());
        doNothing().when(transactionManager).rollback(any());
    }

    @Test
    void createOrder_success_withProduct() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(10);

        OrderItemRequestDTO itemReq = new OrderItemRequestDTO();
        itemReq.setProductId(5);
        List<OrderItemRequestDTO> items = new ArrayList<>();
        items.add(itemReq);
        req.setOrderItems(items);

        Account acc = new Account();
        acc.setCustomerId(10);
        when(accountRepository.findById(10)).thenReturn(Optional.of(acc));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Product product = new Product();
        product.setProductId(5);
        when(productRepository.findById(5)).thenReturn(Optional.of(product));

        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(orderMapper.toOrderResponse(any())).thenReturn(new OrderResponse());

        OrderResponse result = orderService.createOrder(req);

        assertThat(result).isNotNull();
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    void createOrder_accountNotFound_throwsException() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(99);

        when(accountRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createOrder(req));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_productNotFound_throwsException() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(10);
        OrderItemRequestDTO itemReq = new OrderItemRequestDTO();
        itemReq.setProductId(5);
        req.setOrderItems(List.of(itemReq));

        when(accountRepository.findById(10)).thenReturn(Optional.of(new Account()));

        when(productRepository.findById(5)).thenReturn(Optional.empty());
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(req));
        assertThat(ex.getMessage()).contains("Product not found");
    }

    @Test
    void createOrder_withoutProductId_throwsException() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(10);
        OrderItemRequestDTO itemReq = new OrderItemRequestDTO(); // productId is null
        req.setOrderItems(List.of(itemReq));

        when(accountRepository.findById(10)).thenReturn(Optional.of(new Account()));
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        assertThrows(RuntimeException.class, () -> orderService.createOrder(req));
    }
}
