package com.example.PixelMageEcomerceProject.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.PixelMageEcomerceProject.dto.response.AchievementResponse;
import com.example.PixelMageEcomerceProject.dto.response.UserAchievementResponse;
import com.example.PixelMageEcomerceProject.entity.Achievement;
import com.example.PixelMageEcomerceProject.entity.UserAchievement;
import com.example.PixelMageEcomerceProject.enums.AchievementConditionType;
import com.example.PixelMageEcomerceProject.mapper.AchievementMapper;
import com.example.PixelMageEcomerceProject.repository.AchievementRepository;
import com.example.PixelMageEcomerceProject.repository.UserAchievementRepository;
import com.example.PixelMageEcomerceProject.repository.UserCollectionProgressRepository;
import com.example.PixelMageEcomerceProject.repository.UserInventoryRepository;
import com.example.PixelMageEcomerceProject.service.impl.AchievementServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.PmPointWalletService;

/**
 * Unit tests for AchievementServiceImpl — Sprint 2.4 TASK-03.
 * Covers 6 scenarios per spec + done condition #6 (row never deleted).
 */
@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private UserInventoryRepository userInventoryRepository;
    @Mock private UserCollectionProgressRepository userCollectionProgressRepository;
    @Mock PmPointWalletService pmPointWalletService;
    @Mock private AchievementMapper achievementMapper;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    private Achievement cardCountAchievement;
    private Achievement hiddenCollectionAchievement;

    @BeforeEach
    void setUp() {
        cardCountAchievement = new Achievement();
        cardCountAchievement.setId(1L);
        cardCountAchievement.setName("Collector Novice");
        cardCountAchievement.setDescription("Sở hữu 5 thẻ bài");
        cardCountAchievement.setConditionType(AchievementConditionType.CARD_COUNT);
        cardCountAchievement.setConditionValue(1); // threshold = 1 for test simplicity
        cardCountAchievement.setPmPointReward(50);
        cardCountAchievement.setIsHidden(false);

        hiddenCollectionAchievement = new Achievement();
        hiddenCollectionAchievement.setId(3L);
        hiddenCollectionAchievement.setName("Secret Archivist");
        hiddenCollectionAchievement.setDescription("???");
        hiddenCollectionAchievement.setConditionType(AchievementConditionType.COLLECTION_COMPLETE);
        hiddenCollectionAchievement.setConditionValue(1);
        hiddenCollectionAchievement.setPmPointReward(500);
        hiddenCollectionAchievement.setIsHidden(true);
    }

    /**
     * Done condition #4 — link card → checkAndGrantAchievements() fires → UserAchievement row created.
     */
    @Test
    void checkAndGrantAchievements_cardCountMet_grantsAchievement() {
        when(achievementRepository.findAllNotYetEarnedByUser(100))
                .thenReturn(List.of(cardCountAchievement));
        when(userInventoryRepository.countByUser_CustomerIdAndQuantityGreaterThan(100, 0)).thenReturn(3);

        achievementService.checkAndGrantAchievements(100);

        verify(userAchievementRepository).save(any(UserAchievement.class));
    }

    /**
     * Condition not yet met — no grant.
     */
    @Test
    void checkAndGrantAchievements_conditionNotMet_noGrant() {
        when(achievementRepository.findAllNotYetEarnedByUser(100))
                .thenReturn(List.of(cardCountAchievement));
        when(userInventoryRepository.countByUser_CustomerIdAndQuantityGreaterThan(100, 0)).thenReturn(0);

        achievementService.checkAndGrantAchievements(100);

        verify(userAchievementRepository, never()).save(any());
    }

    /**
     * Done condition #4 edge — no candidates returned means no double-grant possible.
     */
    @Test
    void checkAndGrantAchievements_alreadyEarned_noDoubleGrant() {
        when(achievementRepository.findAllNotYetEarnedByUser(100)).thenReturn(List.of());

        achievementService.checkAndGrantAchievements(100);

        verify(userAchievementRepository, never()).save(any());
    }

    /**
     * Done condition #5 — unlink card → revokeIfConditionNotMet() fires → isActive set to false.
     * Done condition #6 — row is NEVER deleted (verify never().delete()).
     */
    @Test
    void revokeIfConditionNotMet_conditionFailed_setsInactive() {
        UserAchievement activeGrant = new UserAchievement();
        activeGrant.setId(1L);
        activeGrant.setUserId(100);
        activeGrant.setAchievement(cardCountAchievement);
        activeGrant.setIsActive(true);

        when(userAchievementRepository.findByUserIdAndIsActiveTrue(100))
                .thenReturn(List.of(activeGrant));
        // Inventory now empty → condition fails
        when(userInventoryRepository.countByUser_CustomerIdAndQuantityGreaterThan(100, 0)).thenReturn(0);

        achievementService.revokeIfConditionNotMet(100);

        // isActive must be false on save
        verify(userAchievementRepository).save(argThat(ua -> !ua.getIsActive()));
        // Done condition #6: row NEVER deleted
        verify(userAchievementRepository, never()).delete(any());
        verify(userAchievementRepository, never()).deleteById(any());
    }

    /**
     * Condition still met → no revoke.
     */
    @Test
    void revokeIfConditionNotMet_conditionStillMet_keepsActive() {
        UserAchievement activeGrant = new UserAchievement();
        activeGrant.setId(1L);
        activeGrant.setUserId(100);
        activeGrant.setAchievement(cardCountAchievement);
        activeGrant.setIsActive(true);

        when(userAchievementRepository.findByUserIdAndIsActiveTrue(100))
                .thenReturn(List.of(activeGrant));
        // Condition still met
        when(userInventoryRepository.countByUser_CustomerIdAndQuantityGreaterThan(100, 0)).thenReturn(5);

        achievementService.revokeIfConditionNotMet(100);

        verify(userAchievementRepository, never()).save(any());
    }

    /**
     * Done condition #7 — hidden achievement NOT earned → must not appear in getAllAchievements.
     */
    @Test
    void getAllAchievements_hiddenNotEarned_notInList() {
        when(achievementRepository.findAll())
                .thenReturn(List.of(cardCountAchievement, hiddenCollectionAchievement));
        // cardCountAchievement: not earned
        when(userAchievementRepository.existsByUserIdAndAchievement_IdAndIsActiveTrue(100, 1L)).thenReturn(false);
        // hiddenCollectionAchievement: not earned either
        when(userAchievementRepository.existsByUserIdAndAchievement_IdAndIsActiveTrue(100, 3L)).thenReturn(false);

        AchievementResponse mockResponse1 = new AchievementResponse();
        mockResponse1.setId(1L);
        when(achievementMapper.toAchievementResponse(any(Achievement.class))).thenAnswer(invocation -> {
            Achievement arg = invocation.getArgument(0);
            if (arg.getId() == 1L) return mockResponse1;
            return new AchievementResponse();
        });

        List<AchievementResponse> result = achievementService.getAllAchievements(100);

        // Only the non-hidden unearned achievement is shown; hidden + unearned must be absent
        assertNotNull(result);
        assert result.stream().noneMatch(r -> r.getId() != null && r.getId().equals(3L)) : "Hidden unearned should not appear";
        assert result.stream().anyMatch(r -> r.getId() != null && r.getId().equals(1L)) : "Non-hidden should appear";
    }

    /**
     * Done condition #8 — hidden achievement EARNED → must appear in getMyAchievements.
     */
    @Test
    void getMyAchievements_hiddenEarned_visible() {
        UserAchievement earnedHidden = new UserAchievement();
        earnedHidden.setId(10L);
        earnedHidden.setUserId(100);
        earnedHidden.setAchievement(hiddenCollectionAchievement);
        earnedHidden.setIsActive(true);

        UserAchievementResponse mockMappedResponse = new UserAchievementResponse();
        mockMappedResponse.setAchievementId(3);
        mockMappedResponse.setEarned(true);

        when(userAchievementRepository.findByUserIdAndIsActiveTrue(100))
                .thenReturn(List.of(earnedHidden));
        when(achievementMapper.toUserAchievementResponse(any(UserAchievement.class))).thenReturn(mockMappedResponse);

        List<UserAchievementResponse> result = achievementService.getMyAchievements(100);

        assertNotNull(result);
        assert !result.isEmpty() : "Earned hidden should appear in /my";
        assert result.get(0).getAchievementId().equals(3) : "Should be the hidden achievement";
        assert Boolean.TRUE.equals(result.get(0).getEarned());
    }

    // Helper to make argThat more readable
    private static <T> T argThat(java.util.function.Predicate<T> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
