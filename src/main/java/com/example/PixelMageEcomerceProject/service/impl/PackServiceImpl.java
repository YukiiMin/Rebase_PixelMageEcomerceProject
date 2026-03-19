package com.example.PixelMageEcomerceProject.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.example.PixelMageEcomerceProject.service.interfaces.PackService;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import com.example.PixelMageEcomerceProject.exceptions.InsufficientCardsException;
import java.security.SecureRandom;

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

    private static final int CARDS_PER_PACK = 5;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final CardTemplateRarity[] SLOT_RARITY = {
        CardTemplateRarity.COMMON,   // Slot 1 — guaranteed
        CardTemplateRarity.COMMON,   // Slot 2 — guaranteed
        CardTemplateRarity.COMMON,   // Slot 3 — guaranteed
        null,                        // Slot 4 — roll: 70% COMMON / 30% RARE
        null                         // Slot 5 — roll: 80% RARE  / 20% LEGENDARY
    };

    @Override
    @Transactional
    public Pack createPack(PackRequestDTO requestDTO) {
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + requestDTO.getProductId()));

        Account createdBy = null;
        if (requestDTO.getCreatedByAccountId() != null) {
            createdBy = accountRepository.findById(requestDTO.getCreatedByAccountId())
                    .orElseThrow(() -> new RuntimeException(
                            "Account not found with id: " + requestDTO.getCreatedByAccountId()));
        }

        // 1. Create Pack entity
        Pack pack = new Pack();
        pack.setProduct(product);
        pack.setStatus(PackStatus.STOCKED);
        pack.setCreatedBy(createdBy);
        pack = packRepository.save(pack);

        List<PackDetail> packDetails = new ArrayList<>();

        for (int slot = 0; slot < CARDS_PER_PACK; slot++) {
            CardTemplateRarity targetRarity = resolveSlotRarity(slot);
            Card selected = drawCardByRarity(targetRarity);

            // Card stays READY - status unchanged here
            // 3. Create Pack Detail linking Pack -> Card
            PackDetail detail = new PackDetail();
            detail.setPack(pack);
            detail.setCard(selected);
            detail.setPositionIndex(slot + 1);
            packDetails.add(detail);
        }

        packDetailRepository.saveAll(packDetails);
        pack.setPackDetails(packDetails);
        return packRepository.save(pack);
    }

    private CardTemplateRarity resolveSlotRarity(int slotIndex) {
        if (SLOT_RARITY[slotIndex] != null) return SLOT_RARITY[slotIndex];

        double roll = secureRandom.nextDouble();
        if (slotIndex == 3) {  // Slot 4: 70/30
            return roll < 0.30 ? CardTemplateRarity.RARE : CardTemplateRarity.COMMON;
        } else {               // Slot 5: 80/20
            return roll < 0.20 ? CardTemplateRarity.LEGENDARY : CardTemplateRarity.RARE;
        }
    }

    private Card drawCardByRarity(CardTemplateRarity rarity) {
        List<Card> pool = cardRepository
            .findByCardTemplate_RarityAndStatus(rarity, CardProductStatus.READY);

        if (pool.isEmpty()) {
            // Fallback chain: LEGENDARY → RARE → COMMON → throw
            if (rarity == CardTemplateRarity.LEGENDARY) {
                log.warn("LEGENDARY pool exhausted — falling back to RARE");
                pool = cardRepository.findByCardTemplate_RarityAndStatus(
                    CardTemplateRarity.RARE, CardProductStatus.READY);
            }
            if (pool.isEmpty() && rarity != CardTemplateRarity.COMMON) {
                log.warn("RARE pool exhausted — falling back to COMMON");
                pool = cardRepository.findByCardTemplate_RarityAndStatus(
                    CardTemplateRarity.COMMON, CardProductStatus.READY);
            }
            if (pool.isEmpty()) {
                throw new InsufficientCardsException(
                    "Không đủ thẻ READY trong kho để tạo Pack. Vui lòng bổ sung thẻ.");
            }
        }
        return pool.get(secureRandom.nextInt(pool.size()));
    }

    @Override
    public Pack updatePackStatus(Integer packId, String status) {
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("Pack not found: " + packId));
        pack.setStatus(PackStatus.valueOf(status));
        return packRepository.save(pack);
    }

    @Override
    public Optional<Pack> getPackById(Integer id) {
        return packRepository.findById(id);
    }

    @Override
    public List<Pack> getAllPacks() {
        return packRepository.findAll();
    }

    @Override
    public List<Pack> getPacksByStatus(String status) {
        return packRepository.findByStatus(status);
    }

    @Override
    public List<Pack> getPacksByProductAndStatus(Integer productId, String status) {
        return packRepository.findByProductProductIdAndStatus(productId, status);
    }

    @Override
    public void deletePack(Integer id) {
        packRepository.deleteById(id);
    }
}
