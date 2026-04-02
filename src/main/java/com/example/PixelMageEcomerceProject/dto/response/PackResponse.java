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
    private Integer productId;
    private String productName;
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
        private Integer positionIndex;
    }
}
