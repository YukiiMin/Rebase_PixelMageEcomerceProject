package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private List<TarotSessionStat> tarotSessions;
    private List<SpreadUsageStat> spreadUsage;
    private List<UserRegistrationStat> userRegistrations;
    private List<RarityDistributionStat> rarityDistribution;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TarotSessionStat {
        private String date; // YYYY-MM-DD
        private Long started;
        private Long completed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpreadUsageStat {
        private String name;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegistrationStat {
        private String date; // YYYY-MM-DD
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RarityDistributionStat {
        private String rarity;
        private Long count;
    }
}
