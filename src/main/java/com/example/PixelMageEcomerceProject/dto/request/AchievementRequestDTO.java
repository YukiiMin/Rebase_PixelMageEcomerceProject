package com.example.PixelMageEcomerceProject.dto.request;

import com.example.PixelMageEcomerceProject.enums.AchievementConditionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementRequestDTO {
    private String name;
    private String description;
    private AchievementConditionType conditionType;
    private Integer conditionValue;
    private Integer pmPointReward;
    private Boolean isHidden;
}
