package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResponse {
    private Integer collectionId;
    private String collectionName;
    private String description;
    private String collectionType;
    private Boolean isPublic;
    private String source;
    private String rewardType;
    private Integer ownedCount;
    private Integer requiredCount;
    private Double completionPercent;
    private Boolean isCompleted;
    private List<CollectionItemResponse> items;
}
