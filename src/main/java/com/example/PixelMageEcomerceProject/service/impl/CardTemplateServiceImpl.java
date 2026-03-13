package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.CardTemplateRequestDTO;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
@Transactional
public class CardTemplateServiceImpl implements CardTemplateService {

    private final CardTemplateRepository cardTemplateRepository;

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
    public Optional<CardTemplate> getCardTemplateByName(String name) {
        return cardTemplateRepository.findByName(name);
    }
}
