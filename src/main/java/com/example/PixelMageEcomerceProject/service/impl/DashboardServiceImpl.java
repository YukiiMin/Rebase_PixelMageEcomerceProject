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

import com.example.PixelMageEcomerceProject.repository.CardContentRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingSessionRepository;
import com.example.PixelMageEcomerceProject.repository.SpreadRepository;
import com.example.PixelMageEcomerceProject.dto.response.AnalyticsResponse;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionStatus;
import com.example.PixelMageEcomerceProject.mapper.OrderMapper;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.Spread;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final CardContentRepository cardContentRepository;
    private final PackRepository packRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final SpreadRepository spreadRepository;
    // Assume OrderMapper is available, if not we will just map manually or inject it.
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        Long totalUsers = accountRepository.count();
        Long totalOrders = orderRepository.count();
        Long totalCardTemplates = cardTemplateRepository.count();
        Long totalCardContents = cardContentRepository.count();
        Long totalPacks = packRepository.count();

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
                if (item.getPack() != null && item.getPack().getPackCategory() != null) {
                    String packName = item.getPack().getPackCategory().getName();
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
                .totalCardContents(totalCardContents)
                .totalPacks(totalPacks)
                .totalRevenue(totalRevenue)
                .revenueByDay(revenueByDay)
                .revenueByPackType(revenueByPackType)
                .recentOrders(recentOrders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 1. Tarot Sessions stats
        List<ReadingSession> allSessions = readingSessionRepository.findAll();
        Map<String, AnalyticsResponse.TarotSessionStat> sessionMap = new HashMap<>();
        for (ReadingSession session : allSessions) {
            String dateStr = session.getCreatedAt().format(formatter);
            AnalyticsResponse.TarotSessionStat stat = sessionMap.computeIfAbsent(dateStr, k -> new AnalyticsResponse.TarotSessionStat(k, 0L, 0L));
            stat.setStarted(stat.getStarted() + 1);
            if (session.getStatus() == ReadingSessionStatus.COMPLETED) {
                stat.setCompleted(stat.getCompleted() + 1);
            }
        }
        List<AnalyticsResponse.TarotSessionStat> tarotSessions = new ArrayList<>(sessionMap.values());
        tarotSessions.sort(Comparator.comparing(AnalyticsResponse.TarotSessionStat::getDate));

        // 2. Spread Usage
        List<Spread> spreads = spreadRepository.findAll();
        List<AnalyticsResponse.SpreadUsageStat> spreadUsage = new ArrayList<>();
        // Efficient way: count sessions by spread. Since we already loaded allSessions, group by them:
        Map<Integer, Long> spreadCounts = allSessions.stream()
                .filter(s -> s.getSpread() != null)
                .collect(Collectors.groupingBy(s -> s.getSpread().getSpreadId(), Collectors.counting()));
        for (Spread sp : spreads) {
            spreadUsage.add(new AnalyticsResponse.SpreadUsageStat(sp.getName(), spreadCounts.getOrDefault(sp.getSpreadId(), 0L)));
        }

        // 3. User Registrations
        List<Account> accounts = accountRepository.findAll();
        Map<String, Long> userRegMap = new TreeMap<>(); // TreeMap keeps it sorted
        for (Account acc : accounts) {
            if (acc.getCreatedAt() != null) {
                String dateStr = acc.getCreatedAt().format(formatter);
                userRegMap.put(dateStr, userRegMap.getOrDefault(dateStr, 0L) + 1);
            }
        }
        List<AnalyticsResponse.UserRegistrationStat> userRegistrations = userRegMap.entrySet().stream()
                .map(e -> new AnalyticsResponse.UserRegistrationStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 4. Rarity Distribution
        List<CardTemplate> templates = cardTemplateRepository.findAll();
        Map<String, Long> rarityMap = new HashMap<>();
        for (CardTemplate ct : templates) {
            if (ct.getRarity() != null) {
                String rarity = ct.getRarity().name();
                rarityMap.put(rarity, rarityMap.getOrDefault(rarity, 0L) + 1);
            }
        }
        List<AnalyticsResponse.RarityDistributionStat> rarityDistribution = rarityMap.entrySet().stream()
                .map(e -> new AnalyticsResponse.RarityDistributionStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .tarotSessions(tarotSessions)
                .spreadUsage(spreadUsage)
                .userRegistrations(userRegistrations)
                .rarityDistribution(rarityDistribution)
                .build();
    }
}
