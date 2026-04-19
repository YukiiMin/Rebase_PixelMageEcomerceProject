package com.example.PixelMageEcomerceProject.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.PackRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.PackDetail;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.PackDetailRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.repository.PackCategoryRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.PackService;
import com.example.PixelMageEcomerceProject.exceptions.InsufficientCardsException;
import com.example.PixelMageEcomerceProject.mapper.PackMapper;
import com.example.PixelMageEcomerceProject.dto.response.PackResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PackServiceImpl implements PackService {

    private final PackRepository packRepository;
    private final PackDetailRepository packDetailRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final PackCategoryRepository packCategoryRepository;
    private final PackMapper packMapper;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true),
        @CacheEvict(value = "products",                     allEntries = true),
        @CacheEvict(value = "products-public",              allEntries = true),
        @CacheEvict(value = "product-by-id",                allEntries = true)
    })
    public PackResponse createPack(PackRequestDTO requestDTO) {
        // Obsolete
        return null;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true),
        @CacheEvict(value = "products",                     allEntries = true),
        @CacheEvict(value = "products-public",              allEntries = true),
        @CacheEvict(value = "product-by-id",                allEntries = true)
    })
    public List<PackResponse> generatePacks(Integer packCategoryId, Integer quantity) {
        com.example.PixelMageEcomerceProject.entity.PackCategory category = packCategoryRepository.findById(packCategoryId)
                .orElseThrow(() -> new RuntimeException("PackCategory not found"));

        List<com.example.PixelMageEcomerceProject.entity.CardTemplate> pool = category.getCardPools();
        if (pool == null || pool.isEmpty()) {
            throw new RuntimeException("This PackCategory has no CardTemplate pools assigned.");
        }

        Map<String, Integer> rarityRates;
        try {
            ObjectMapper mapper = new ObjectMapper();
            rarityRates = mapper.readValue(category.getRarityRates(), new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Invalid rarity_rates JSON format in PackCategory.", e);
        }

        List<Pack> newlyGeneratedPacks = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < quantity; i++) {
            Pack newPack = new Pack();
            newPack.setPackCategory(category);
            newPack.setStatus(PackStatus.STOCKED); // Initial status
            newPack.setPackDetails(new ArrayList<>());
            Pack savedPack = packRepository.save(newPack);

            for (int k = 0; k < category.getCardsPerPack(); k++) {
                com.example.PixelMageEcomerceProject.entity.CardTemplate selectedTemplate = rollRarityAndPickTemplate(rarityRates, pool, random);
                
                PackDetail pd = new PackDetail();
                pd.setPack(savedPack);
                pd.setCardTemplate(selectedTemplate);
                // card field is nullable because the physical card hasn't been scanned/assigned yet
                packDetailRepository.save(pd);
                
                savedPack.getPackDetails().add(pd);
            }
            newlyGeneratedPacks.add(savedPack);
        }
        
        return newlyGeneratedPacks.stream().map(packMapper::toResponse).toList();
    }

    private com.example.PixelMageEcomerceProject.entity.CardTemplate rollRarityAndPickTemplate(Map<String, Integer> rarityRates, List<com.example.PixelMageEcomerceProject.entity.CardTemplate> pool, Random random) {
        // Roll rarity
        int totalWeight = rarityRates.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) totalWeight = 100;
        
        int roll = random.nextInt(totalWeight) + 1; // 1 to totalWeight
        String selectedRarity = "COMMON";
        int currentSum = 0;
        
        for (Map.Entry<String, Integer> entry : rarityRates.entrySet()) {
            currentSum += entry.getValue();
            if (roll <= currentSum) {
                selectedRarity = entry.getKey();
                break;
            }
        }
        
        // Filter pool by rarity
        String finalRarity = selectedRarity;
        List<com.example.PixelMageEcomerceProject.entity.CardTemplate> availableTemplates = pool.stream()
            .filter(ct -> ct.getRarity().name().equalsIgnoreCase(finalRarity))
            .toList();
            
        if (availableTemplates.isEmpty()) {
            // fallback if no card of that rarity exists
            availableTemplates = pool; 
        }
        
        return availableTemplates.get(random.nextInt(availableTemplates.size()));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true),
        @CacheEvict(value = "products",                     allEntries = true),
        @CacheEvict(value = "products-public",              allEntries = true),
        @CacheEvict(value = "product-by-id",                allEntries = true)
    })
    public PackResponse updatePackStatus(Integer packId, PackStatus status) {
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("Pack not found: " + packId));
        pack.setStatus(status);
        return packMapper.toResponse(packRepository.save(pack));
    }

    @Override
    public Optional<PackResponse> getPackById(Integer id) {
        return packRepository.findById(id).map(packMapper::toResponse);
    }

    @Override
    @Cacheable(value = "packs")
    public List<PackResponse> getAllPacks() {
        return packRepository.findAll().stream().map(packMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "packs-by-status", key = "#status")
    public List<PackResponse> getPacksByStatus(PackStatus status) {
        return packRepository.findByStatus(status).stream().map(packMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "packs-by-product-status", key = "#packCategoryId + '-' + #status")
    public List<PackResponse> getPacksByProductAndStatus(Integer packCategoryId, PackStatus status) {
        return packRepository.findByPackCategoryPackCategoryIdAndStatus(packCategoryId, status).stream().map(packMapper::toResponse).toList();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true),
        @CacheEvict(value = "products",                     allEntries = true),
        @CacheEvict(value = "products-public",              allEntries = true),
        @CacheEvict(value = "product-by-id",                allEntries = true)
    })
    public void deletePack(Integer id) {
        packRepository.deleteById(id);
    }
}
