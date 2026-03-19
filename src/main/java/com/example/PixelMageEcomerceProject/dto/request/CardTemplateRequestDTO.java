package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import com.example.PixelMageEcomerceProject.enums.Suit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardTemplateRequestDTO {
    private String name;
    private String description;
    private String designPath;
    private ArcanaType arcanaType;
    private Suit suit;
    private Integer cardNumber;
    private CardTemplateRarity rarity;
    private String imagePath;
    private Integer frameworkId;
}

