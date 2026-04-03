package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;

import com.example.PixelMageEcomerceProject.dto.response.AchievementResponse;
import com.example.PixelMageEcomerceProject.dto.response.UserAchievementResponse;

/**
 * AchievementService — Stub Architecture for Sprint 2.4.
 * Fires only on discrete NFC scan events (linkCard / unlinkCard).
 * Phase 2: swap impl for event-consumer without breaking this interface.
 */
public interface AchievementService {

    // CRUD for Admin/Staff
    AchievementResponse createAchievement(com.example.PixelMageEcomerceProject.dto.request.AchievementRequestDTO requestDTO);
    AchievementResponse updateAchievement(Long id, com.example.PixelMageEcomerceProject.dto.request.AchievementRequestDTO requestDTO);
    void deleteAchievement(Long id);

    /**
     * Called after linkCard — checks all unearned achievements and grants
     * those whose condition is now met.
     */
    void checkAndGrantAchievements(Integer userId);

    /**
     * Called after unlinkCard — revokes active achievements whose
     * condition is no longer met (soft revoke: isActive = false, row kept).
     */
    void revokeIfConditionNotMet(Integer userId);

    /**
     * GET /api/achievements — all non-hidden achievements + hidden ones already earned by user.
     */
    List<AchievementResponse> getAllAchievements(Integer userId);

    /**
     * GET /api/achievements/my — only earned (isActive=true) achievements for user,
     * including hidden ones.
     */
    List<UserAchievementResponse> getMyAchievements(Integer userId);
}
