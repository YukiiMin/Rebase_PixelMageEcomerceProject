package com.example.PixelMageEcomerceProject.dto.request;

import com.example.PixelMageEcomerceProject.enums.ContentType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardContentRequestDTO {

    @NotNull(message = "cardTemplateId is required")
    private Integer cardTemplateId;

    /** Heading for the content block (optional) */
    private String title;

    @NotNull(message = "contentType is required (STORY | IMAGE | VIDEO | GIF | LINK)")
    private ContentType contentType;

    @NotBlank(message = "contentData must not be blank")
    private String contentData;

    @Min(value = 1, message = "displayOrder must be >= 1")
    private Integer displayOrder = 1;

    /** Defaults true on create; Admin can set false to hide */
    private Boolean isActive = true;
}
