package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.PixelMageEcomerceProject.dto.request.CardTemplateRequestDTO;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;

@Service
public interface CardTemplateService {
    CardTemplate createCardTemplate(CardTemplateRequestDTO cardTemplateRequestDTO);

    CardTemplate updateCardTemplate(Integer id, CardTemplateRequestDTO cardTemplateRequestDTO);

    void deleteCardTemplate(Integer id);

    Optional<CardTemplate> getCardTemplateById(Integer id);

    List<CardTemplate> getAllCardTemplates();
    List<CardTemplate> getAllVisibleCardTemplates();

    Page<CardTemplate> getAllCardTemplates(Pageable pageable);
    Page<CardTemplate> getAllVisibleCardTemplates(Pageable pageable);

    Page<CardTemplate> getAllByRarity(CardTemplateRarity rarity, Pageable pageable);
    Page<CardTemplate> getAllVisibleByRarity(CardTemplateRarity rarity, Pageable pageable);

    Page<CardTemplate> getAllByArcana(ArcanaType arcanaType, Pageable pageable);
    Page<CardTemplate> getAllVisibleByArcana(ArcanaType arcanaType, Pageable pageable);

    Page<CardTemplate> getAllByFramework(Integer frameworkId, Pageable pageable);
    Page<CardTemplate> getAllVisibleByFramework(Integer frameworkId, Pageable pageable);

    Optional<CardTemplate> getCardTemplateByName(String name);

    CardTemplate toggleVisibility(Integer id);

    Page<CardTemplate> searchCardTemplates(String search, CardTemplateRarity rarity, Integer frameworkId, boolean includeInvisible, Pageable pageable);
}
