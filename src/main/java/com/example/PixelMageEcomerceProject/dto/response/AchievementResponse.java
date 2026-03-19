package com.example.PixelMageEcomerceProject.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Achievement response DTO — Sprint 2.4 Stub Architecture.
 * No progress % per architecture decision (Pass/Fail only).
 * Phase 2: progressNumerator, progressDenominator will be added here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isEarned;
    private LocalDateTime grantedAt;
}
