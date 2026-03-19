package com.example.PixelMageEcomerceProject.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.example.PixelMageEcomerceProject.enums.CollectionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCollectionRequestDTO {
    private String collectionName;
    private String description;
    private CollectionType collectionType; // STANDARD / LIMITED / ACHIEVEMENT / HIDDEN
    private LocalDateTime startTime; // required if LIMITED
    private LocalDateTime endTime;   // required if LIMITED
    private Boolean isVisible;
    private String rewardType;
    private String rewardData;
    private List<AdminCollectionItemRequestDTO> items;
}

