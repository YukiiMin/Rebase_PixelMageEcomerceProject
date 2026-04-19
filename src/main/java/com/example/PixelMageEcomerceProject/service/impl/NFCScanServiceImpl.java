package com.example.PixelMageEcomerceProject.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.exceptions.CardLockedInSessionException;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingCardRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.AchievementService;
import com.example.PixelMageEcomerceProject.service.interfaces.NFCScanService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NFCScanServiceImpl implements NFCScanService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final UserInventoryService userInventoryService;
    private final WebSocketNotificationService wsNotificationService;
    private final ReadingCardRepository readingCardRepository;
    private final AchievementService achievementService;

    @Override
    public Map<String, Object> scanNFC(String nfcUid, String softwareUuid, Integer userId) {
        Card card = cardRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // Only validate softwareUuid for staff operations (when provided)
        if (softwareUuid != null && (card.getSoftwareUuid() == null || !card.getSoftwareUuid().equals(softwareUuid))) {
            throw new RuntimeException("Card validation failed: Anti-cloning check rejected the card.");
        }

        Map<String, Object> response = new HashMap<>();

        CardProductStatus status = card.getStatus();
        if (CardProductStatus.PENDING_BIND.equals(status)
                || CardProductStatus.DEACTIVATED.equals(status)) {
            throw new RuntimeException("Card is " + status + ", cannot be scanned");
        }

        if (CardProductStatus.READY.equals(status) || CardProductStatus.SOLD.equals(status)) {
            response.put("action", "LINK_PROMPT");
            response.put("card_info", card);
            return response;
        }

        if (CardProductStatus.LINKED.equals(status)) {
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
    public Map<String, Object> linkCard(String nfcUid, String softwareUuid, Integer userId) {
        Card card = cardRepository.findLockedByNfcUid(nfcUid)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // Only validate softwareUuid for staff operations (when provided)
        if (softwareUuid != null && (card.getSoftwareUuid() == null || !card.getSoftwareUuid().equals(softwareUuid))) {
            throw new RuntimeException("Card validation failed: Anti-cloning check rejected the card.");
        }

        if (card.getCardTemplate() != null) {
            boolean isInActiveSession = readingCardRepository
                    .existsByCardTemplate_CardTemplateIdAndReadingSession_StatusIn(
                            card.getCardTemplate().getCardTemplateId(),
                            Arrays.asList("PENDING", "INTERPRETING"));

            if (isInActiveSession) {
                throw new CardLockedInSessionException(
                        "Card đang được dùng trong phiên đọc bài chưa hoàn thành. " +
                                "Phiên phải kết thúc trước khi thực hiện thao tác này.");
            }
        }

        if (!CardProductStatus.READY.equals(card.getStatus())
                && !CardProductStatus.SOLD.equals(card.getStatus())) {
            throw new RuntimeException("Cannot link card with status: " + card.getStatus());
        }

        Account owner = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        card.setStatus(CardProductStatus.LINKED);
        card.setOwner(owner);
        card.setLinkedAt(LocalDateTime.now());

        cardRepository.save(card);

        // Cập nhật Inventory +1 theo template ID
        if (card.getCardTemplate() != null) {
            userInventoryService.upsertInventory(userId, card.getCardTemplate().getCardTemplateId(), 1);
        }

        // Step 5: checkAndGrantAchievements (Sprint 2.4 TASK-03)
        achievementService.checkAndGrantAchievements(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Card linked successfully");
        response.put("card_info", card);

        // Push real-time event đến FE/MO
        wsNotificationService.pushToUser(userId, NotificationEvent.nfcLinked(userId, Map.of(
                "cardId", card.getCardId(),
                "nfcUid", nfcUid,
                "cardTemplateName", card.getCardTemplate() != null ? card.getCardTemplate().getName() : "")));

        return response;
    }

    @Override
    public Map<String, Object> unlinkCard(String nfcUid, Integer userId) {
        Card card = cardRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getCardTemplate() != null) {
            boolean isInActiveSession = readingCardRepository
                    .existsByCardTemplate_CardTemplateIdAndReadingSession_StatusIn(
                            card.getCardTemplate().getCardTemplateId(),
                            Arrays.asList("PENDING", "INTERPRETING"));

            if (isInActiveSession) {
                throw new CardLockedInSessionException(
                        "Card đang được dùng trong phiên đọc bài chưa hoàn thành. " +
                                "Phiên phải kết thúc trước khi thực hiện thao tác này.");
            }
        }

        if (!CardProductStatus.LINKED.equals(card.getStatus())) {
            throw new RuntimeException("Card is not linked yet");
        }

        if (card.getOwner() == null || !card.getOwner().getCustomerId().equals(userId)) {
            throw new RuntimeException("You are not the owner of this card");
        }

        card.setStatus(CardProductStatus.READY); // Reset owner
        card.setOwner(null);
        card.setLinkedAt(null);

        cardRepository.save(card);

        // Cập nhật Inventory -1 theo template ID
        if (card.getCardTemplate() != null) {
            userInventoryService.upsertInventory(userId, card.getCardTemplate().getCardTemplateId(), -1);
        }

        // Step 5: revokeIfConditionNotMet (Sprint 2.4 TASK-03)
        achievementService.revokeIfConditionNotMet(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Card unlinked successfully");
        response.put("card_info", card);

        // Push real-time event đến FE/MO
        wsNotificationService.pushToUser(userId, NotificationEvent.nfcUnlinked(userId, Map.of(
                "cardId", card.getCardId(),
                "nfcUid", nfcUid)));

        return response;
    }
}
