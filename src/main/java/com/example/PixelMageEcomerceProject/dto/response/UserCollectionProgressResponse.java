package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCollectionProgressResponse {
    private Integer collectionId;
    private String collectionName;
    private String collectionType;
    private Boolean isPublic;
    private Integer ownedCount;
    private Integer requiredCount;
    private Double completionPercent;
    private Boolean isCompleted;
    private String rewardType;
}
