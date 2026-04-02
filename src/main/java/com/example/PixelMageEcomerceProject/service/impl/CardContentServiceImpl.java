package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.request.CardContentRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.CardContentResponse;
import com.example.PixelMageEcomerceProject.entity.CardContent;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.repository.CardContentRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.mapper.CardContentMapper;
import com.example.PixelMageEcomerceProject.service.interfaces.CardContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CardContentServiceImpl implements CardContentService {

    private final CardContentRepository cardContentRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final CardContentMapper cardContentMapper;

    // ── Write Operations ────────────────────────────────────────────────────

    @Override
    @Caching(evict = {
        @CacheEvict(value = "card-contents-active", key = "#dto.cardTemplateId"),
        @CacheEvict(value = "card-contents-all",    key = "#dto.cardTemplateId")
    })
    public CardContentResponse createCardContent(CardContentRequestDTO dto) {
        CardTemplate template = cardTemplateRepository.findById(dto.getCardTemplateId())
                .orElseThrow(() -> new RuntimeException(
                        "CardTemplate not found with id: " + dto.getCardTemplateId()));

        CardContent content = CardContent.builder()
                .cardTemplate(template)
                .title(dto.getTitle())
                .contentType(dto.getContentType())
                .contentData(dto.getContentData())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 1)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        return cardContentMapper.toResponse(cardContentRepository.save(content));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "card-contents-active", allEntries = true),
        @CacheEvict(value = "card-contents-all",    allEntries = true)
    })
    public CardContentResponse updateCardContent(Integer id, CardContentRequestDTO dto) {
        CardContent existing = cardContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CardContent not found with id: " + id));

        if (dto.getCardTemplateId() != null &&
            !dto.getCardTemplateId().equals(existing.getCardTemplate().getCardTemplateId())) {
            CardTemplate template = cardTemplateRepository.findById(dto.getCardTemplateId())
                    .orElseThrow(() -> new RuntimeException(
                            "CardTemplate not found with id: " + dto.getCardTemplateId()));
            existing.setCardTemplate(template);
        }

        if (dto.getTitle() != null)        existing.setTitle(dto.getTitle());
        if (dto.getContentType() != null)  existing.setContentType(dto.getContentType());
        if (dto.getContentData() != null)  existing.setContentData(dto.getContentData());
        if (dto.getDisplayOrder() != null) existing.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getIsActive() != null)     existing.setIsActive(dto.getIsActive());

        return cardContentMapper.toResponse(cardContentRepository.save(existing));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "card-contents-active", allEntries = true),
        @CacheEvict(value = "card-contents-all",    allEntries = true)
    })
    public CardContentResponse toggleActive(Integer id, boolean isActive) {
        CardContent existing = cardContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CardContent not found with id: " + id));
        existing.setIsActive(isActive);
        return cardContentMapper.toResponse(cardContentRepository.save(existing));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "card-contents-active", allEntries = true),
        @CacheEvict(value = "card-contents-all",    allEntries = true)
    })
    public void deleteCardContent(Integer id) {
        if (!cardContentRepository.existsById(id)) {
            throw new RuntimeException("CardContent not found with id: " + id);
        }
        cardContentRepository.deleteById(id);
    }

    // ── Read Operations ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<CardContentResponse> getCardContentById(Integer id) {
        return cardContentRepository.findById(id).map(cardContentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "card-contents-active", key = "#cardTemplateId")
    public List<CardContentResponse> getActiveContentsByTemplateId(Integer cardTemplateId) {
        return cardContentMapper.toResponses(cardContentRepository
                .findByCardTemplateCardTemplateIdAndIsActiveTrueOrderByDisplayOrderAsc(cardTemplateId));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "card-contents-all", key = "#cardTemplateId")
    public List<CardContentResponse> getAllContentsByTemplateId(Integer cardTemplateId) {
        return cardContentMapper.toResponses(cardContentRepository
                .findByCardTemplateCardTemplateIdOrderByDisplayOrderAsc(cardTemplateId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardContentResponse> getAllCardContents() {
        return cardContentMapper.toResponses(cardContentRepository.findAll());
    }
}
