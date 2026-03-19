package com.example.PixelMageEcomerceProject.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.response.AchievementResponse;
import com.example.PixelMageEcomerceProject.entity.Achievement;
import com.example.PixelMageEcomerceProject.entity.UserAchievement;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import com.example.PixelMageEcomerceProject.repository.AchievementRepository;
import com.example.PixelMageEcomerceProject.repository.UserAchievementRepository;
import com.example.PixelMageEcomerceProject.repository.UserCollectionProgressRepository;
import com.example.PixelMageEcomerceProject.repository.UserInventoryRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.AchievementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AchievementServiceImpl — Sprint 2.4 Stub Architecture.
 * Fires only on discrete NFC scan events (linkCard / unlinkCard) — no polling, no streaming.
 * Pass/Fail only — no % progress tracking.
 * Phase 2: replace this impl with an event-consumer without breaking the AchievementService contract.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserCollectionProgressRepository userCollectionProgressRepository;

    @Override
    public void checkAndGrantAchievements(Integer userId) {
        List<Achievement> candidates = achievementRepository.findAllNotYetEarnedByUser(userId);

        for (Achievement achievement : candidates) {
            boolean conditionMet = evaluateCondition(userId, achievement);
            if (conditionMet) {
                UserAchievement grant = new UserAchievement();
                grant.setUserId(userId);
                grant.setAchievement(achievement);
                grant.setGrantedAt(LocalDateTime.now());
                grant.setIsActive(true);
                userAchievementRepository.save(grant);
                log.info("Achievement granted: userId={}, achievementId={}, name={}",
                        userId, achievement.getId(), achievement.getName());

                // Trigger PM_point earn — uncomment after TASK-04 (PmPointWallet) is complete
                // pmPointWalletService.credit(userId, achievement.getPmPointReward());
            }
        }
    }

    @Override
    public void revokeIfConditionNotMet(Integer userId) {
        List<UserAchievement> active = userAchievementRepository.findByUserIdAndIsActiveTrue(userId);

        for (UserAchievement ua : active) {
            boolean stillMet = evaluateCondition(userId, ua.getAchievement());
            if (!stillMet) {
                ua.setIsActive(false);
                userAchievementRepository.save(ua);
                log.info("Achievement revoked: userId={}, achievementId={}, name={}",
                        userId, ua.getAchievement().getId(), ua.getAchievement().getName());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements(Integer userId) {
        List<Achievement> all = achievementRepository.findAll();
        List<AchievementResponse> result = new ArrayList<>();

        for (Achievement a : all) {
            boolean isEarned = userAchievementRepository
                    .existsByUserIdAndAchievement_IdAndIsActiveTrue(userId, a.getId());

            // Hidden achievements only appear if already earned
            if (Boolean.TRUE.equals(a.getIsHidden()) && !isEarned) {
                continue;
            }

            LocalDateTime grantedAt = null;
            if (isEarned) {
                Optional<UserAchievement> ua = userAchievementRepository
                        .findByUserIdAndAchievement_Id(userId, a.getId());
                grantedAt = ua.map(UserAchievement::getGrantedAt).orElse(null);
            }

            result.add(new AchievementResponse(a.getId(), a.getName(), a.getDescription(),
                    isEarned, grantedAt));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> getMyAchievements(Integer userId) {
        List<UserAchievement> earned = userAchievementRepository.findByUserIdAndIsActiveTrue(userId);
        List<AchievementResponse> result = new ArrayList<>();

        for (UserAchievement ua : earned) {
            Achievement a = ua.getAchievement();
            result.add(new AchievementResponse(a.getId(), a.getName(), a.getDescription(),
                    true, ua.getGrantedAt()));
        }

        return result;
    }

    /**
     * Evaluates whether the given achievement's condition is currently met for the user.
     *
     * CARD_COUNT: user owns >= conditionValue total linked card templates.
     * RARITY_COUNT: Sprint 2.4 hardcoded to count LEGENDARY cards.
     *               Phase 2 TODO: add conditionRarity field to Achievement entity.
     * COLLECTION_COMPLETE: user has completed >= conditionValue collections (isCompleted = true).
     */
    private boolean evaluateCondition(Integer userId, Achievement achievement) {
        return switch (achievement.getConditionType()) {
            case CARD_COUNT -> {
                int totalLinked = userInventoryRepository
                        .countByUser_CustomerIdAndQuantityGreaterThan(userId, 0);
                yield totalLinked >= achievement.getConditionValue();
            }
            case RARITY_COUNT -> {
                // Sprint 2.4: RARITY_COUNT counts LEGENDARY cards only.
                // Phase 2 TODO: generalize using Achievement.conditionRarity field.
                long legendaryCount = userInventoryRepository
                        .findByUser_CustomerIdAndQuantityGreaterThan(userId, 0)
                        .stream()
                        .filter(inv -> inv.getCardTemplate() != null
                                && CardTemplateRarity.LEGENDARY.equals(
                                        inv.getCardTemplate().getRarity()))
                        .count();
                yield legendaryCount >= achievement.getConditionValue();
            }
            case COLLECTION_COMPLETE -> {
                long completedCount = userCollectionProgressRepository
                        .findByUser_CustomerId(userId)
                        .stream()
                        .filter(p -> Boolean.TRUE.equals(p.getIsCompleted()))
                        .count();
                yield completedCount >= achievement.getConditionValue();
            }
        };
    }
}
