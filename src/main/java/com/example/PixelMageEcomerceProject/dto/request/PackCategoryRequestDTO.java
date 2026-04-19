package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackCategoryRequestDTO {
    private String name;
    private String description;
    private String imageUrl;
    private Integer cardsPerPack;
    private String rarityRates; // JSON string
    private Boolean isActive;
    private List<Integer> cardTemplateIds; // For relating M-N CardTemplates
}
