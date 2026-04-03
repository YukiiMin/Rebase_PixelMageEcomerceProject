package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardTemplateRepository extends JpaRepository<CardTemplate, Integer> {
    Optional<CardTemplate> findByName(String name);
    Page<CardTemplate> findByRarity(CardTemplateRarity rarity, Pageable pageable);
    Page<CardTemplate> findByArcanaType(ArcanaType arcanaType, Pageable pageable);
}
