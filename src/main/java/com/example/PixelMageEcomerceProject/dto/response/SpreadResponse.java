package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpreadResponse {
    private Integer spreadId;
    private String name;
    private String description;
    private Integer positionCount;
    private Integer minCardsRequired;
}
