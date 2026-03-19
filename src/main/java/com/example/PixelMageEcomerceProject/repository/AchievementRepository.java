package com.example.PixelMageEcomerceProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Achievement;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    /**
     * Returns all achievements NOT yet actively earned by the given user.
     * Used by checkAndGrantAchievements to find candidates to evaluate.
     */
    @Query("SELECT a FROM Achievement a WHERE a.id NOT IN " +
           "(SELECT ua.achievement.id FROM UserAchievement ua " +
           " WHERE ua.userId = :userId AND ua.isActive = true)")
    List<Achievement> findAllNotYetEarnedByUser(@Param("userId") Integer userId);
}
