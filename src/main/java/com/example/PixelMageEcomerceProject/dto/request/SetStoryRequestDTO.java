package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetStoryRequestDTO {
    private String title;
    private String content;
    private String requiredTemplateIds;
    private String coverImagePath;
    private Boolean isActive;
}

