package com.example.PixelMageEcomerceProject.enums;

/**
 * Condition types for Achievement evaluation (Sprint 2.4 Stub Architecture).
 * Phase 2: RARITY_COUNT will be generalized with a conditionRarity field on Achievement.
 * Sprint 2.4: RARITY_COUNT is hardcoded to count LEGENDARY cards only.
 */
public enum AchievementConditionType {
    CARD_COUNT,           // user owns >= conditionValue total linked cards
    RARITY_COUNT,         // Sprint 2.4: user owns >= conditionValue LEGENDARY cards (hardcoded)
    COLLECTION_COMPLETE   // user has completed >= conditionValue collections (isCompleted = true)
}
