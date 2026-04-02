package com.example.PixelMageEcomerceProject.dto.response;

import com.example.PixelMageEcomerceProject.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardContentResponse {
    private Integer contentId;
    private Integer cardTemplateId;
    private String  cardTemplateName;
    private String  title;
    private ContentType contentType;
    private String  contentData;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
