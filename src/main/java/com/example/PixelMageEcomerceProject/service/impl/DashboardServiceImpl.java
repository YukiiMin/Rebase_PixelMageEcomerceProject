package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.response.DashboardResponse;
import com.example.PixelMageEcomerceProject.dto.response.OrderResponse;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.entity.OrderItem;
import com.example.PixelMageEcomerceProject.enums.PaymentStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final CardTemplateRepository cardTemplateRepository;
    // Assume OrderMapper is available, if not we will just map manually or inject it.
    private final com.example.PixelMageEcomerceProject.mapper.OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        Long totalUsers = accountRepository.count();
        Long totalOrders = orderRepository.count();
        Long totalCardTemplates = cardTemplateRepository.count();

        // 1. Total Revenue (PaymentStatus = SUCCEEDED)
        List<Order> succeededOrders = orderRepository.findByPaymentStatus(PaymentStatus.SUCCEEDED);
        BigDecimal totalRevenue = succeededOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Revenue By Day (Last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Order> recentSucceededOrders = orderRepository.findByPaymentStatusAndOrderDateAfter(PaymentStatus.SUCCEEDED, sevenDaysAgo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, BigDecimal> dailyMap = new HashMap<>();
        
        for (Order order : recentSucceededOrders) {
            String dayStr = order.getOrderDate().format(formatter);
            dailyMap.put(dayStr, dailyMap.getOrDefault(dayStr, BigDecimal.ZERO).add(order.getTotalAmount()));
        }

        List<DashboardResponse.DailyRevenue> revenueByDay = dailyMap.entrySet().stream()
                .map(e -> new DashboardResponse.DailyRevenue(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardResponse.DailyRevenue::getDate))
                .collect(Collectors.toList());

        // 3. Revenue By Pack Type
        Map<String, BigDecimal> packTypeMap = new HashMap<>();
        for (Order order : succeededOrders) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getPack() != null && item.getPack().getProduct() != null) {
                    String packName = item.getPack().getProduct().getName();
                    packTypeMap.put(packName, packTypeMap.getOrDefault(packName, BigDecimal.ZERO).add(item.getSubtotal()));
                }
            }
        }
        List<DashboardResponse.PackTypeRevenue> revenueByPackType = packTypeMap.entrySet().stream()
                .map(e -> new DashboardResponse.PackTypeRevenue(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 4. Recent Orders (Top 5) — sorted by createdAt (@CreationTimestamp, confirmed on Order entity)
        List<OrderResponse> recentOrders = orderRepository.findAll(
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalCardTemplates(totalCardTemplates)
                .totalRevenue(totalRevenue)
                .revenueByDay(revenueByDay)
                .revenueByPackType(revenueByPackType)
                .recentOrders(recentOrders)
                .build();
    }
}
