package com.example.PixelMageEcomerceProject.dto.response;

import com.example.PixelMageEcomerceProject.enums.PackStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackResponse {
    private Integer packId;
    private Integer packCategoryId;
    private String packCategoryName;
    private String packCategoryImageUrl;   // enriched — for monitoring display
    private PackStatus status;
    private Integer createdByAccountId;
    private LocalDateTime createdAt;
    private List<Detail> packDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private Integer cardId;
        private String cardName;
        private String rarity;          // enriched — from CardTemplate.rarity
        private String imagePath;       // enriched — from CardTemplate.imagePath
        private Integer positionIndex;
        private String cardStatus;      // enriched — from Card.status
        private String nfcUid;          // enriched — from Card.nfcUid
    }
}
