package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
    Page<CardTemplate> findByCardFramework_FrameworkId(String frameworkId, Pageable pageable);
}
