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
public class ProductResponse {
    private Integer productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer poolSize;
    private Integer stockCount;
    private List<CardTemplateResponse.Summary> poolPreview;
    
    private com.example.PixelMageEcomerceProject.enums.ProductType productType;
    private Boolean isVisible;
    private Boolean isActive;
    private Integer packCategoryId;
    private Integer cardTemplateId;
}
