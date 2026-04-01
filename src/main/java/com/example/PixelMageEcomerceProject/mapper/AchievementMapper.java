package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.AchievementResponse;
import com.example.PixelMageEcomerceProject.dto.response.UserAchievementResponse;
import com.example.PixelMageEcomerceProject.entity.Achievement;
import com.example.PixelMageEcomerceProject.entity.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AchievementMapper {

    @Mapping(target = "isEarned", constant = "false")
    @Mapping(target = "grantedAt", ignore = true)
    AchievementResponse toAchievementResponse(Achievement achievement);

    @Mapping(target = "achievementId", source = "achievement.id")
    @Mapping(target = "name", source = "achievement.name")
    @Mapping(target = "description", source = "achievement.description")
    @Mapping(target = "conditionType", source = "achievement.conditionType")
    @Mapping(target = "conditionValue", source = "achievement.conditionValue")
    @Mapping(target = "pmPointReward", source = "achievement.pmPointReward")
    @Mapping(target = "isHidden", source = "achievement.isHidden")
    @Mapping(target = "earned", source = "isActive")
    UserAchievementResponse toUserAchievementResponse(UserAchievement userAchievement);

}
