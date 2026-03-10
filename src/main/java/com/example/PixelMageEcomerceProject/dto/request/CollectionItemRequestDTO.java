package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItemRequestDTO {
    private Integer collectionId;
    private Integer cardTemplateId;
    private Integer requiredQuantity = 1;
}
