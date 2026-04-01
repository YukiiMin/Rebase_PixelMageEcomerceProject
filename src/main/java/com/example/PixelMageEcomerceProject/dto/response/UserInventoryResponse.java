package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInventoryResponse {
    private Integer cardTemplateId;
    private String name;
    private String arcanaType;
    private String rarity;
    private String imagePath;
    private Integer quantity;
    private Integer linkedCardCount;
}
