package com.example.PixelMageEcomerceProject.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.NFCScanService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NFCScanServiceImpl implements NFCScanService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final UserInventoryService userInventoryService;

    @Override
    public Map<String, Object> scanNFC(String nfcUid, Integer userId) {
        Card card = cardRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        Map<String, Object> response = new HashMap<>();

        String status = card.getStatus();
        if (CardProductStatus.PENDING_BIND.name().equals(status)
                || CardProductStatus.DEACTIVATED.name().equals(status)) {
            throw new RuntimeException("Card is " + status + ", cannot be scanned");
        }

        if (CardProductStatus.READY.name().equals(status) || CardProductStatus.SOLD.name().equals(status)) {
            response.put("action", "LINK_PROMPT");
            response.put("card_info", card);
            return response;
        }

        if (CardProductStatus.LINKED.name().equals(status)) {
            if (card.getOwner() != null && card.getOwner().getCustomerId().equals(userId)) {
                response.put("action", "VIEW_CONTENT");
                response.put("card_info", card);
                return response;
            } else {
                throw new RuntimeException("Card belongs to another account");
            }
        }

        throw new RuntimeException("Unknown status");
    }

    @Override
    public Map<String, Object> linkCard(String nfcUid, Integer userId) {
        Card card = cardRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!CardProductStatus.READY.name().equals(card.getStatus())
                && !CardProductStatus.SOLD.name().equals(card.getStatus())) {
            throw new RuntimeException("Cannot link card with status: " + card.getStatus());
        }

        Account owner = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        card.setStatus(CardProductStatus.LINKED.name());
        card.setOwner(owner);
        card.setLinkedAt(LocalDateTime.now());

        cardRepository.save(card);

        // Cập nhật Inventory +1 theo template ID
        if (card.getCardTemplate() != null) {
            userInventoryService.upsertInventory(userId, card.getCardTemplate().getCardTemplateId(), 1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Card linked successfully");
        response.put("card_info", card);

        return response;
    }

    @Override
    public Map<String, Object> unlinkCard(String nfcUid, Integer userId) {
        Card card = cardRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!CardProductStatus.LINKED.name().equals(card.getStatus())) {
            throw new RuntimeException("Card is not linked yet");
        }

        if (card.getOwner() == null || !card.getOwner().getCustomerId().equals(userId)) {
            throw new RuntimeException("You are not the owner of this card");
        }

        card.setStatus(CardProductStatus.READY.name()); // Reset owner
        card.setOwner(null);
        card.setLinkedAt(null);

        cardRepository.save(card);

        // Cập nhật Inventory -1 theo template ID
        if (card.getCardTemplate() != null) {
            userInventoryService.upsertInventory(userId, card.getCardTemplate().getCardTemplateId(), -1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Card unlinked successfully");
        response.put("card_info", card);
        return response;
    }
}
