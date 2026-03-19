package com.example.PixelMageEcomerceProject.entity;

import com.example.PixelMageEcomerceProject.enums.AchievementConditionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Achievement definition — seeded by Staff/Admin, not created by users.
 * Phase 2 note: progressNumerator/Denominator will be added when moving off Stub Architecture.
 * Phase 2 note: RARITY_COUNT conditionRarity field will be added to generalize beyond LEGENDARY.
 */
@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private AchievementConditionType conditionType;

    /**
     * Threshold value for the condition.
     * CARD_COUNT: total linked cards needed.
     * RARITY_COUNT: Sprint 2.4 = number of LEGENDARY cards needed.
     * COLLECTION_COMPLETE: number of collections to complete.
     */
    @Column(name = "condition_value", nullable = false)
    private Integer conditionValue;

    @Column(name = "pm_point_reward", nullable = false)
    private Integer pmPointReward = 0;

    /**
     * Hidden achievements are not listed in GET /api/achievements until earned.
     * They ARE visible in GET /api/achievements/my once earned.
     */
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
}
