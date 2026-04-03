package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.CardTemplateRequestDTO;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.ArcanaType;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CardTemplateService {
    CardTemplate createCardTemplate(CardTemplateRequestDTO cardTemplateRequestDTO);
    CardTemplate updateCardTemplate(Integer id, CardTemplateRequestDTO cardTemplateRequestDTO);
    void deleteCardTemplate(Integer id);
    Optional<CardTemplate> getCardTemplateById(Integer id);
    List<CardTemplate> getAllCardTemplates();
    Page<CardTemplate> getAllCardTemplates(Pageable pageable);
    Page<CardTemplate> getAllByRarity(CardTemplateRarity rarity, Pageable pageable);
    Page<CardTemplate> getAllByArcana(ArcanaType arcanaType, Pageable pageable);
    Optional<CardTemplate> getCardTemplateByName(String name);
}

