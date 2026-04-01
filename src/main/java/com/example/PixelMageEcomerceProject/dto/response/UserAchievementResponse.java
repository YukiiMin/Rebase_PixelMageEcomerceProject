package com.example.PixelMageEcomerceProject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementResponse {
    private Integer achievementId;
    private String name;
    private String description;
    private String conditionType;
    private Integer conditionValue;
    private Integer pmPointReward;
    private Boolean isHidden;
    private Boolean earned;
    private LocalDateTime grantedAt;
}
