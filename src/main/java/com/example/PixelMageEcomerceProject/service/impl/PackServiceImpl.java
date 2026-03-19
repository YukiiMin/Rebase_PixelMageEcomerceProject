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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PackServiceImpl implements PackService {

    private final PackRepository packRepository;
    private final PackDetailRepository packDetailRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    private static final int CARDS_PER_PACK = 3;

    @Override
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
        pack.setStatus(PackStatus.CREATED);
        pack.setCreatedBy(createdBy);
        pack = packRepository.save(pack);

        // 2. Perform RNG to select physical cards (Must be READY)
        List<Card> readyCards = cardRepository.findByStatus(CardProductStatus.READY);
        if (readyCards.size() < CARDS_PER_PACK) {
            throw new RuntimeException(
                    "Not enough physical cards in READY status to form a pack. Found: " + readyCards.size());
        }

        // Shuffle existing cards and pick CARDS_PER_PACK
        java.util.Collections.shuffle(readyCards);
        List<Card> selectedCards = readyCards.subList(0, CARDS_PER_PACK);

        List<PackDetail> packDetails = new ArrayList<>();

        for (int i = 0; i < CARDS_PER_PACK; i++) {
            Card card = selectedCards.get(i);

            // Update card status -> SOLD (reserved for pack)
            card.setStatus(CardProductStatus.SOLD);
            cardRepository.save(card);

            // 3. Create Pack Detail linking Pack -> Card
            PackDetail detail = new PackDetail();
            detail.setPack(pack);
            detail.setCard(card);
            detail.setPositionIndex(i + 1);
            packDetails.add(detail);
        }

        packDetailRepository.saveAll(packDetails);
        pack.setPackDetails(packDetails);

        // 4. Update Pack status -> STOCKED
        pack.setStatus(PackStatus.STOCKED);
        pack = packRepository.save(pack);

        return pack;
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
