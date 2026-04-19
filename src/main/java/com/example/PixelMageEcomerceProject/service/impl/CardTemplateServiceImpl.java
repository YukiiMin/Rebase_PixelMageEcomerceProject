package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.CardTemplateRequestDTO;
import com.example.PixelMageEcomerceProject.entity.CardFramework;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import com.example.PixelMageEcomerceProject.repository.CardFrameworkRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CardTemplateServiceImpl implements CardTemplateService {

    private final CardTemplateRepository cardTemplateRepository;
    private final CardFrameworkRepository cardFrameworkRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "card-templates", allEntries = true),
            @CacheEvict(value = "card-template-by-id", allEntries = true)
    })
    public CardTemplate createCardTemplate(CardTemplateRequestDTO cardTemplateRequestDTO) {
        CardTemplate cardTemplate = new CardTemplate();
        cardTemplate.setName(cardTemplateRequestDTO.getName());
        cardTemplate.setDescription(cardTemplateRequestDTO.getDescription());
        cardTemplate.setDesignPath(cardTemplateRequestDTO.getDesignPath());
        cardTemplate.setArcanaType(cardTemplateRequestDTO.getArcanaType());
        cardTemplate.setSuit(cardTemplateRequestDTO.getSuit());
        cardTemplate.setCardNumber(cardTemplateRequestDTO.getCardNumber());
        cardTemplate.setRarity(cardTemplateRequestDTO.getRarity());
        cardTemplate.setImagePath(cardTemplateRequestDTO.getImagePath());

        if (cardTemplateRequestDTO.getFrameworkId() != null) {
            CardFramework framework = cardFrameworkRepository.findById(cardTemplateRequestDTO.getFrameworkId())
                    .orElseThrow(() -> new RuntimeException(
                            "CardFramework not found with id: " + cardTemplateRequestDTO.getFrameworkId()));
            cardTemplate.setCardFramework(framework);
        }

        return cardTemplateRepository.save(cardTemplate);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "card-templates", allEntries = true),
            @CacheEvict(value = "card-template-by-id", key = "#id")
    })
    public CardTemplate updateCardTemplate(Integer id, CardTemplateRequestDTO cardTemplateRequestDTO) {
        Optional<CardTemplate> existingTemplate = cardTemplateRepository.findById(id);
        if (existingTemplate.isPresent()) {
            CardTemplate updatedTemplate = existingTemplate.get();
            updatedTemplate.setName(cardTemplateRequestDTO.getName());
            updatedTemplate.setDescription(cardTemplateRequestDTO.getDescription());
            updatedTemplate.setDesignPath(cardTemplateRequestDTO.getDesignPath());
            updatedTemplate.setArcanaType(cardTemplateRequestDTO.getArcanaType());
            updatedTemplate.setSuit(cardTemplateRequestDTO.getSuit());
            updatedTemplate.setCardNumber(cardTemplateRequestDTO.getCardNumber());
            updatedTemplate.setRarity(cardTemplateRequestDTO.getRarity());
            updatedTemplate.setImagePath(cardTemplateRequestDTO.getImagePath());

            if (cardTemplateRequestDTO.getFrameworkId() != null) {
                CardFramework framework = cardFrameworkRepository.findById(cardTemplateRequestDTO.getFrameworkId())
                        .orElseThrow(() -> new RuntimeException(
                                "CardFramework not found with id: " + cardTemplateRequestDTO.getFrameworkId()));
                updatedTemplate.setCardFramework(framework);
            }

            return cardTemplateRepository.save(updatedTemplate);
        }
        throw new RuntimeException("CardTemplate not found with id: " + id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "card-templates", allEntries = true),
            @CacheEvict(value = "card-template-by-id", key = "#id")
    })
    public void deleteCardTemplate(Integer id) {
        CardTemplate template = cardTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CardTemplate not found with id: " + id));
        template.setActive(false);
        cardTemplateRepository.save(template);
    }

    @Override
    @Cacheable(value = "card-template-by-id", key = "#id")
    public Optional<CardTemplate> getCardTemplateById(Integer id) {
        return cardTemplateRepository.findById(id);
    }

    @Override
    @Cacheable("card-templates")
    public List<CardTemplate> getAllCardTemplates() {
        return cardTemplateRepository.findAll();
    }

    @Override
    @Cacheable("card-templates-visible")
    public List<CardTemplate> getAllVisibleCardTemplates() {
        return cardTemplateRepository.findByIsVisibleTrue();
    }

    /**
     * Pageable variant — NOT cached (page/sort params make cache keys ambiguous)
     */
    @Override
    public Page<CardTemplate> getAllCardTemplates(Pageable pageable) {
        return cardTemplateRepository.findAll(pageable);
    }

    @Override
    public Page<CardTemplate> getAllVisibleCardTemplates(Pageable pageable) {
        return cardTemplateRepository.findByIsVisibleTrue(pageable);
    }

    @Override
    public Page<CardTemplate> getAllByRarity(CardTemplateRarity rarity, Pageable pageable) {
        return cardTemplateRepository.findByRarity(rarity, pageable);
    }

    @Override
    public Page<CardTemplate> getAllVisibleByRarity(CardTemplateRarity rarity, Pageable pageable) {
        return cardTemplateRepository.findByRarityAndIsVisibleTrue(rarity, pageable);
    }

    @Override
    public Page<CardTemplate> getAllByArcana(ArcanaType arcanaType, Pageable pageable) {
        return cardTemplateRepository.findByArcanaType(arcanaType, pageable);
    }

    @Override
    public Page<CardTemplate> getAllVisibleByArcana(ArcanaType arcanaType, Pageable pageable) {
        return cardTemplateRepository.findByArcanaTypeAndIsVisibleTrue(arcanaType, pageable);
    }

    @Override
    public Page<CardTemplate> getAllByFramework(Integer frameworkId, Pageable pageable) {
        return cardTemplateRepository.findByCardFramework_FrameworkId(frameworkId, pageable);
    }

    @Override
    public Page<CardTemplate> getAllVisibleByFramework(Integer frameworkId, Pageable pageable) {
        return cardTemplateRepository.findByCardFramework_FrameworkIdAndIsVisibleTrue(frameworkId, pageable);
    }

    @Override
    public Optional<CardTemplate> getCardTemplateByName(String name) {
        return cardTemplateRepository.findByName(name);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "card-templates",      allEntries = true),
            @CacheEvict(value = "card-template-by-id", key = "#id")
    })
    public CardTemplate toggleVisibility(Integer id) {
        // dùng findByIdIgnoreActive để bypass @SQLRestriction("is_active = true")
        CardTemplate template = cardTemplateRepository.findByIdIgnoreActive(id)
                .orElseThrow(() -> new RuntimeException("CardTemplate not found with id: " + id));
        template.setIsVisible(
                template.getIsVisible() == null || !template.getIsVisible()
        );
        return cardTemplateRepository.save(template);
    }
}
