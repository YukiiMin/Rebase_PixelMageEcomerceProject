package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.CardContentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.CardContentResponse;

import java.util.List;
import java.util.Optional;

public interface CardContentService {

    // ── Write Operations (ADMIN / STAFF) ────────────────────────────────────

    /** Create a new content block for a CardTemplate */
    CardContentResponse createCardContent(CardContentRequestDTO dto);

    /** Update an existing content block */
    CardContentResponse updateCardContent(Integer id, CardContentRequestDTO dto);

    /** Toggle isActive (soft-hide without deleting) */
    CardContentResponse toggleActive(Integer id, boolean isActive);

    /** Hard delete a content block (Admin only) */
    void deleteCardContent(Integer id);

    // ── Read Operations ──────────────────────────────────────────────────────

    /**
     * Get a single content block by ID.
     * Returns regardless of isActive (Admin needs to see all states).
     */
    Optional<CardContentResponse> getCardContentById(Integer id);

    /**
     * Get all ACTIVE content blocks for a CardTemplate, ordered by displayOrder.
     * Used by public gallery (no auth required).
     * Results are cached in Redis.
     */
    List<CardContentResponse> getActiveContentsByTemplateId(Integer cardTemplateId);

    /**
     * Get ALL content blocks (active + inactive) for a CardTemplate.
     * Used by Admin management panel.
     */
    List<CardContentResponse> getAllContentsByTemplateId(Integer cardTemplateId);

    /**
     * Get all content records across all templates (Admin overview).
     */
    List<CardContentResponse> getAllCardContents();
}
