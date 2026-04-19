package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private com.example.PixelMageEcomerceProject.enums.ProductType productType;
    private Boolean isVisible;
    private Boolean isActive;
    private Integer packCategoryId;
    private Integer cardTemplateId;
    private Integer initialStock;
}
