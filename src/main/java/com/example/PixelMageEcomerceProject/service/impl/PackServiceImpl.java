package com.example.PixelMageEcomerceProject.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final PackMapper packMapper;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true)
    })
    public PackResponse createPack(PackRequestDTO requestDTO) {
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + requestDTO.getProductId()));

        Account createdBy = null;
        if (requestDTO.getCreatedByAccountId() != null) {
            createdBy = accountRepository.findById(requestDTO.getCreatedByAccountId())
                    .orElseThrow(() -> new RuntimeException(
                            "Account not found with id: " + requestDTO.getCreatedByAccountId()));
        }

        List<Integer> cardIds = requestDTO.getCardIds();
        if (cardIds == null || cardIds.isEmpty()) {
            throw new RuntimeException("Card IDs list is required to create a pack");
        }

        // 3. Validate số card khớp với SKU
        if (product.getProductId() == 1 && cardIds.size() != 5) {
            throw new RuntimeException("Standard Pack (id=1) must have exactly 5 cards");
        }
        if (product.getProductId() == 2 && cardIds.size() != 16) {
            throw new RuntimeException("Blister Promo (id=2) must have exactly 16 cards");
        }
        if (product.getProductId() == 3 && cardIds.size() != 50) {
            throw new RuntimeException("Major Sealed Box (id=3) must have exactly 50 cards");
        }

        // 2. Validate: tất cả Card phải là READY và thuộc productId đúng
        List<Card> cards = cardRepository.findAllById(cardIds);
        if (cards.size() != cardIds.size()) {
            throw new RuntimeException("Some cards were not found based on provided IDs");
        }
        for (Card card : cards) {
            if (card.getStatus() != CardProductStatus.READY) {
                throw new InsufficientCardsException("Card " + card.getCardId() + " is not READY");
            }
            if (!card.getProduct().getProductId().equals(product.getProductId())) {
                throw new RuntimeException("Card " + card.getCardId() + " does not belong to product " + product.getProductId());
            }
        }

        // 1. Create Pack entity
        Pack pack = new Pack();
        pack.setProduct(product);
        pack.setStatus(PackStatus.STOCKED);
        pack.setCreatedBy(createdBy);
        pack = packRepository.save(pack);

        // 4. Tạo Pack Details từ list cardIds
        List<PackDetail> packDetails = new ArrayList<>();
        int slot = 0;
        for (Card card : cards) {
            PackDetail detail = new PackDetail();
            detail.setPack(pack);
            detail.setCard(card);
            detail.setPositionIndex(slot + 1);
            packDetails.add(detail);
            slot++;
        }

        packDetailRepository.saveAll(packDetails);
        pack.setPackDetails(packDetails);
        return packMapper.toResponse(packRepository.save(pack));
    }



    @Override
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true)
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
    @Cacheable(value = "packs-by-product-status", key = "#productId + '-' + #status")
    public List<PackResponse> getPacksByProductAndStatus(Integer productId, PackStatus status) {
        return packRepository.findByProductProductIdAndStatus(productId, status).stream().map(packMapper::toResponse).toList();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "packs",                        allEntries = true),
        @CacheEvict(value = "packs-by-status",              allEntries = true),
        @CacheEvict(value = "packs-by-product-status",      allEntries = true)
    })
    public void deletePack(Integer id) {
        packRepository.deleteById(id);
    }
}
