package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalUsers;
    private Long totalOrders;
    private Long totalCardTemplates;
    private Long totalCardContents;
    private Long totalPacks;
    private BigDecimal totalRevenue;

    private List<DailyRevenue> revenueByDay;
    private List<PackTypeRevenue> revenueByPackType;
    private List<OrderResponse> recentOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenue {
        private String date; // YYYY-MM-DD
        private BigDecimal revenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackTypeRevenue {
        private String packName;
        private BigDecimal revenue;
    }
}
