package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.CardRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final ProductRepository productRepository;

    @Override
    public Card createCardProduct(CardRequestDTO cardRequestDTO) {
        CardTemplate cardTemplate = cardTemplateRepository.findById(cardRequestDTO.getCardTemplateId())
                .orElseThrow(() -> new RuntimeException(
                        "CardTemplate not found with id: " + cardRequestDTO.getCardTemplateId()));

        Product product = productRepository.findById(cardRequestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + cardRequestDTO.getProductId()));

        Card card = new Card();
        card.setCardTemplate(cardTemplate);
        card.setProduct(product);
        card.setCustomText(cardRequestDTO.getCustomText());
        card.setProductionBatch(cardRequestDTO.getProductionBatch());
        card.setCardCondition(cardRequestDTO.getCardCondition() != null ? cardRequestDTO.getCardCondition() : "NEW");
        card.setStatus(CardProductStatus.PENDING_BIND.name());

        return cardRepository.save(card);
    }

    @Override
    public Card bindNFC(Integer cardId, String nfcUid) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        Optional<Card> existing = cardRepository.findByNfcUid(nfcUid);
        if (existing.isPresent() && !existing.get().getCardId().equals(cardId)) {
            throw new RuntimeException("NFC UID already bound to another card");
        }

        card.setNfcUid(nfcUid);
        card.setStatus(CardProductStatus.READY.name());
        return cardRepository.save(card);
    }

    @Override
    public Card updateStatus(Integer cardId, String newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));
        card.setStatus(newStatus);
        return cardRepository.save(card);
    }

    @Override
    public Card updateCard(Integer id, CardRequestDTO cardRequestDTO) {
        Optional<Card> existingCard = cardRepository.findById(id);
        if (existingCard.isPresent()) {
            Card updatedCard = existingCard.get();

            if (cardRequestDTO.getCardTemplateId() != null) {
                CardTemplate cardTemplate = cardTemplateRepository.findById(cardRequestDTO.getCardTemplateId())
                        .orElseThrow(() -> new RuntimeException(
                                "CardTemplate not found with id: " + cardRequestDTO.getCardTemplateId()));
                updatedCard.setCardTemplate(cardTemplate);
            }

            if (cardRequestDTO.getProductId() != null) {
                Product product = productRepository.findById(cardRequestDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException(
                                "Product not found with id: " + cardRequestDTO.getProductId()));
                updatedCard.setProduct(product);
            }

            if (cardRequestDTO.getProductionBatch() != null) {
                updatedCard.setProductionBatch(cardRequestDTO.getProductionBatch());
            }
            if (cardRequestDTO.getCardCondition() != null) {
                updatedCard.setCardCondition(cardRequestDTO.getCardCondition());
            }

            updatedCard.setCustomText(cardRequestDTO.getCustomText());
            return cardRepository.save(updatedCard);
        }
        throw new RuntimeException("Card not found with id: " + id);
    }

    @Override
    public void deleteCard(Integer id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        card.setStatus(CardProductStatus.DEACTIVATED.name());
        cardRepository.save(card);
    }

    @Override
    public Optional<Card> getCardById(Integer id) {
        return cardRepository.findById(id);
    }

    @Override
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    @Override
    public Optional<Card> getCardByNfcUid(String nfcUid) {
        return cardRepository.findByNfcUid(nfcUid);
    }
}
