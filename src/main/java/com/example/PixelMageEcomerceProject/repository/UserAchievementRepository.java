package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.UserAchievement;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserIdAndIsActiveTrue(Integer userId);

    Optional<UserAchievement> findByUserIdAndAchievement_Id(Integer userId, Long achievementId);

    boolean existsByUserIdAndAchievement_IdAndIsActiveTrue(Integer userId, Long achievementId);
}
