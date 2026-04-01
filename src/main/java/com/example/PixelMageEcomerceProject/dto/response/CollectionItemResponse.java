package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItemResponse {
    private Integer collectionItemId;
    private Integer cardTemplateId;
    private String name;
    private String imagePath;
    private String rarity;
    private Boolean owned;
}
