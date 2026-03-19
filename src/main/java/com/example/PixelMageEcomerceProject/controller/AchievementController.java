package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.response.AchievementResponse;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.service.interfaces.AchievementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * List all non-hidden achievements, plus hidden ones already earned by current user.
     * Response includes isEarned + grantedAt so FE can show earned state.
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ResponseBase<List<AchievementResponse>>> getAllAchievements(Authentication auth) {
        Integer userId = extractUserId(auth);
        return ResponseBase.ok(achievementService.getAllAchievements(userId), "Achievements retrieved successfully");
    }

    /**
     * List only achievements earned (isActive = true) by the current user.
     * Includes hidden achievements that have been earned.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ResponseBase<List<AchievementResponse>>> getMyAchievements(Authentication auth) {
        Integer userId = extractUserId(auth);
        return ResponseBase.ok(achievementService.getMyAchievements(userId), "My achievements retrieved successfully");
    }

    private Integer extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Account account) {
            return account.getCustomerId();
        }
        throw new RuntimeException("Could not extract userId from authentication context");
    }
}
