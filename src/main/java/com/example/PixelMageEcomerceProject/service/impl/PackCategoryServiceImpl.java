package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.request.PackCategoryRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PackCategoryResponse;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.PackCategory;
import com.example.PixelMageEcomerceProject.mapper.PackCategoryMapper;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.repository.PackCategoryRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.PackCategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackCategoryServiceImpl implements PackCategoryService {

    private final PackCategoryRepository packCategoryRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final PackCategoryMapper packCategoryMapper;

    @Override
    @Transactional
    public PackCategoryResponse createPackCategory(PackCategoryRequestDTO requestDTO) {
        validateRequest(requestDTO);
        PackCategory category = new PackCategory();
        mapDtoToEntity(requestDTO, category);
        PackCategory saved = packCategoryRepository.save(category);
        return packCategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PackCategoryResponse updatePackCategory(Integer id, PackCategoryRequestDTO requestDTO) {
        validateRequest(requestDTO);
        PackCategory category = packCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pack Category not found"));
        mapDtoToEntity(requestDTO, category);
        PackCategory saved = packCategoryRepository.save(category);
        return packCategoryMapper.toResponse(saved);
    }

    private void validateRequest(PackCategoryRequestDTO requestDTO) {
        if (requestDTO.getName() == null || requestDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên Pack Category không được để trống.");
        }
        if (requestDTO.getCardsPerPack() != null && requestDTO.getCardsPerPack() <= 0) {
            throw new IllegalArgumentException("Số lượng thẻ trong một Pack phải lớn hơn 0.");
        }
        if (requestDTO.getRarityRates() == null || requestDTO.getRarityRates().trim().isEmpty()) {
            throw new IllegalArgumentException("Tỷ lệ phân bổ độ hiếm (rarity_rates) không được để trống.");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Integer> rates = mapper.readValue(requestDTO.getRarityRates(), new TypeReference<Map<String, Integer>>() {});
            int sum = rates.values().stream().mapToInt(Integer::intValue).sum();
            if (sum != 100) {
                throw new IllegalArgumentException("Tổng tỷ lệ độ hiếm phải bằng 100%. (Hiện tại: " + sum + "%).");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Tỷ lệ phân bổ độ hiếm (rarity_rates) phải là định dạng JSON hợp lệ.");
        }
    }

    private void mapDtoToEntity(PackCategoryRequestDTO requestDTO, PackCategory category) {
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setImageUrl(requestDTO.getImageUrl());
        category.setCardsPerPack(requestDTO.getCardsPerPack() != null ? requestDTO.getCardsPerPack() : 5);
        category.setRarityRates(requestDTO.getRarityRates());

        if (requestDTO.getIsActive() != null) {
            category.setIsActive(requestDTO.getIsActive());
        } else if (category.getPackCategoryId() == null) {
            category.setIsActive(true);
        }

        if (requestDTO.getCardTemplateIds() != null && !requestDTO.getCardTemplateIds().isEmpty()) {
            List<CardTemplate> templates = cardTemplateRepository.findAllById(requestDTO.getCardTemplateIds());
            category.setCardPools(templates);
        } else {
            category.setCardPools(null);
        }
    }

    @Override
    @Transactional
    public void deletePackCategory(Integer id) {
        PackCategory category = packCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pack Category not found"));
        // Soft delete
        category.setIsActive(false);
        packCategoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public PackCategoryResponse getPackCategoryById(Integer id) {
        PackCategory category = packCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pack Category not found"));
        return packCategoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackCategoryResponse> getAllPackCategories() {
        return packCategoryRepository.findAll().stream()
                .map(packCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PackCategoryResponse toggleActive(Integer id) {
        PackCategory category = packCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pack Category not found"));
                
        category.setIsActive(!category.getIsActive());
        PackCategory saved = packCategoryRepository.save(category);
        return packCategoryMapper.toResponse(saved);
    }
}
