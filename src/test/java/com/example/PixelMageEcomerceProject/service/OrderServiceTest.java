package com.example.PixelMageEcomerceProject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.PixelMageEcomerceProject.dto.request.OrderItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.OrderRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.OrderItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.service.impl.OrderServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.PaymentService;
import com.example.PixelMageEcomerceProject.service.interfaces.RedisLockService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PackRepository packRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RedisLockService redisLockService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_success_withPack() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(10);

        OrderItemRequestDTO itemReq = new OrderItemRequestDTO();
        itemReq.setPackId(5);
        List<OrderItemRequestDTO> items = new ArrayList<>();
        items.add(itemReq);
        req.setOrderItems(items);

        Account acc = new Account();
        acc.setCustomerId(10);
        when(accountRepository.findById(10)).thenReturn(Optional.of(acc));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Pack pack = new Pack();
        pack.setPackId(5);
        pack.setStatus(PackStatus.STOCKED);
        when(packRepository.findById(5)).thenReturn(Optional.of(pack));
        when(redisLockService.tryLock(any(), anyLong())).thenReturn(true);

        Order result = orderService.createOrder(req);

        assertThat(result).isNotNull();
        assertThat(result.getOrderItems()).hasSize(1);
        assertThat(pack.getStatus()).isEqualTo(PackStatus.RESERVED);
        verify(packRepository, times(1)).save(pack);
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
    void createOrder_packNotStocked_throwsException() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(10);
        OrderItemRequestDTO itemReq = new OrderItemRequestDTO();
        itemReq.setPackId(5);
        req.setOrderItems(List.of(itemReq));

        when(accountRepository.findById(10)).thenReturn(Optional.of(new Account()));
        // when(orderRepository.save(any(Order.class))).thenReturn(new Order()); // Will throw before saving

        Pack pack = new Pack();
        pack.setPackId(5);
        pack.setStatus(PackStatus.RESERVED); // Not STOCKED
        when(packRepository.findById(5)).thenReturn(Optional.of(pack));
        when(redisLockService.tryLock(any(), anyLong())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(req));
        assertThat(ex.getMessage()).contains("Pack is not STOCKED anymore");
    }

    @Test
    void createOrder_withoutPackId_success() {
        OrderRequestDTO req = new OrderRequestDTO();
        req.setCustomerId(10);
        OrderItemRequestDTO itemReq = new OrderItemRequestDTO();
        // packId is null
        req.setOrderItems(List.of(itemReq));

        when(accountRepository.findById(10)).thenReturn(Optional.of(new Account()));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.createOrder(req);

        assertThat(result).isNotNull();
        assertThat(result.getOrderItems()).hasSize(1);
        verify(packRepository, never()).findById(any());
    }
}
