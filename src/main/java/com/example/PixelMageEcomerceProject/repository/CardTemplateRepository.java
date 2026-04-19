package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;

@Repository
public interface CardTemplateRepository extends JpaRepository<CardTemplate, Integer> {

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Optional<CardTemplate> findById(Integer id);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findAll(Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    List<CardTemplate> findAll();

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Optional<CardTemplate> findByName(String name);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByRarity(CardTemplateRarity rarity, Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByArcanaType(ArcanaType arcanaType, Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByCardFramework_FrameworkId(Integer frameworkId, Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByIsVisibleTrue(Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    List<CardTemplate> findByIsVisibleTrue();

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByRarityAndIsVisibleTrue(CardTemplateRarity rarity, Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByArcanaTypeAndIsVisibleTrue(ArcanaType arcanaType, Pageable pageable);

    @EntityGraph(value = "CardTemplate.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<CardTemplate> findByCardFramework_FrameworkIdAndIsVisibleTrue(Integer frameworkId, Pageable pageable);

    /**
     * Bypass @SQLRestriction("is_active = true") — dùng cho admin toggle visibility.
     * Native query đọc thẳng DB không qua Hibernate filter.
     */
    @Query(value = "SELECT * FROM card_templates WHERE card_template_id = :id", nativeQuery = true)
    Optional<CardTemplate> findByIdIgnoreActive(@Param("id") Integer id);
}
