package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.CardContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardContentRepository extends JpaRepository<CardContent, Integer> {

    /** All active content for a CardTemplate, ordered by displayOrder (public gallery) */
    List<CardContent> findByCardTemplateCardTemplateIdAndIsActiveTrueOrderByDisplayOrderAsc(Integer cardTemplateId);

    /** All content (including inactive) for Admin management panel */
    List<CardContent> findByCardTemplateCardTemplateIdOrderByDisplayOrderAsc(Integer cardTemplateId);

    /** Count active contents per template (useful for admin overview) */
    long countByCardTemplateCardTemplateIdAndIsActiveTrue(Integer cardTemplateId);
}
