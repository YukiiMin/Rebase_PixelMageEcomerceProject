package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTemplateResponse {
    private Integer cardTemplateId;
    private String name;
    private String description;
    private String arcanaType;
    private String suit;
    private Integer cardNumber;
    private String rarity;
    private String imagePath;
    private Integer frameworkId;
    private String frameworkName;
    private DivineHelperResponse divineHelper;
}
