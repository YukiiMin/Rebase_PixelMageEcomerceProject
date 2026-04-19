package com.example.PixelMageEcomerceProject.dto.response;

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
public class PackCategoryResponse {
    private Integer packCategoryId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer cardsPerPack;
    private String rarityRates; // JSON string
    private Boolean isActive;

    private List<CardTemplateResponse.Summary> cardPools;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
