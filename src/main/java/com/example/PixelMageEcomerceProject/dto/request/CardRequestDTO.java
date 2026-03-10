package com.example.PixelMageEcomerceProject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequestDTO {
    private String nfcUid;
    private Integer cardTemplateId;
    private Integer productId;
    private String customText;
    private String status;
    private String serialNumber;
    private String productionBatch;
    private String cardCondition;
    private Integer ownerAccountId;
}
