package com.example.PixelMageEcomerceProject.service.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.DivineHelper;
import com.example.PixelMageEcomerceProject.entity.ReadingCard;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.entity.Spread;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.DivineHelperRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingCardRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingSessionRepository;
import com.example.PixelMageEcomerceProject.repository.SpreadRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;
import com.example.PixelMageEcomerceProject.service.interfaces.TarotReadingService;
import org.springframework.cache.annotation.Cacheable;

@Service
public class TarotReadingServiceImpl implements TarotReadingService {

    @Autowired
    private SpreadRepository spreadRepository;

    @Autowired
    private ReadingSessionRepository sessionRepository;

    @Autowired
    private ReadingCardRepository readingCardRepository;

    @Autowired
    private DivineHelperRepository divineHelperRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardTemplateService cardTemplateService;

    @Value("${OPENAI_API_KEY:}")
    private String openAiApiKey;

    @Value("${N8N_WEBHOOK_URL:http://localhost:5678/webhook/tarot-reading}")
    private String n8nWebhookUrl;

    private final SecureRandom secureRandom = new SecureRandom();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Cacheable("spreads")
    public List<Spread> getAllSpreads() {
        return spreadRepository.findAll();
    }

    @Override
    public Map<String, Object> createSession(Integer accountId, Integer spreadId, String mode) {
        // Validate Spread
        Spread spread = spreadRepository.findById(spreadId)
                .orElseThrow(() -> new RuntimeException("Spread not found"));

        // Ensure Account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        ReadingSession session = new ReadingSession();
        session.setAccount(account);
        session.setSpread(spread);
        session.setMode(mode == null ? "EXPLORE" : mode);
        session.setStatus("PENDING");
        ReadingSession savedSession = sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", savedSession.getSessionId());
        response.put("status", savedSession.getStatus());
        response.put("spreadName", spread.getName());
        return response;
    }

    @Override
    public Map<String, Object> drawCards(Integer sessionId, boolean allowReversed) {
        ReadingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"PENDING".equals(session.getStatus())) {
            throw new RuntimeException("Session is already completed or processing. Redraw is not allowed.");
        }

        Spread spread = session.getSpread();
        int cardsToDraw = spread.getPositionCount();

        // Dùng cached card pool thay vì gọi repo trực tiếp (tránh DB query mỗi request)
        List<CardTemplate> allCards = cardTemplateService.getAllCardTemplates();
        if (allCards.size() < cardsToDraw) {
            throw new RuntimeException("Not enough cards in database to draw.");
        }

        // 1. SecureRandom Shuffle
        Collections.shuffle(allCards, secureRandom);

        // 2. Anti-Cheat: Redraw mechanism prevention
        // Save cards to DB BEFORE returning to client
        List<Map<String, Object>> drawnCardsOutput = new ArrayList<>();
        List<ReadingCard> savedCards = new ArrayList<>();

        for (int i = 0; i < cardsToDraw; i++) {
            CardTemplate chosenCard = allCards.get(i);

            ReadingCard readingCard = new ReadingCard();
            readingCard.setReadingSession(session);
            readingCard.setCardTemplate(chosenCard);
            readingCard.setPositionIndex(i + 1);

            // 3. Reversed Cards allow setting
            boolean isReversed = false;
            if (allowReversed) {
                isReversed = secureRandom.nextBoolean(); // 50% chance if allowed
            }
            readingCard.setIsReversed(isReversed);

            ReadingCard savedCard = readingCardRepository.save(readingCard);
            savedCards.add(savedCard);

            Map<String, Object> cardMap = new HashMap<>();
            cardMap.put("cardName", chosenCard.getName());
            cardMap.put("position", savedCard.getPositionIndex());
            cardMap.put("isReversed", isReversed);
            drawnCardsOutput.add(cardMap);
        }

        session.setStatus("CARDS_DRAWN");
        sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("drawnCards", drawnCardsOutput);
        return response;
    }

    @Override
    public Map<String, Object> interpretSession(Integer sessionId) {
        ReadingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<ReadingCard> drawnCards = readingCardRepository.findByReadingSession_SessionId(sessionId);
        if (drawnCards.isEmpty()) {
            throw new RuntimeException("No cards drawn for this session.");
        }

        String interpretation = "";

        // Send to N8N/OpenAI or Fallback
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            // FALLBACK MODE
            interpretation = generateFallbackInterpretation(drawnCards);
        } else {
            // HAPPY PATH: Try N8N Webhook
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("sessionId", sessionId);
                payload.put("spread", session.getSpread().getName());

                List<Map<String, Object>> cardsData = new ArrayList<>();
                for (ReadingCard rc : drawnCards) {
                    Map<String, Object> cData = new HashMap<>();
                    cData.put("cardName", rc.getCardTemplate().getName());
                    cData.put("isReversed", rc.getIsReversed());
                    cData.put("position", rc.getPositionIndex());
                    cardsData.add(cData);
                }
                payload.put("cards", cardsData);

                ResponseEntity<Map> response = restTemplate.postForEntity(n8nWebhookUrl, payload, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    interpretation = (String) response.getBody().get("aiInterpretation");
                } else {
                    interpretation = generateFallbackInterpretation(drawnCards); // N8N timeout/fail fallback
                }
            } catch (Exception e) {
                // Fallback on timeout or connection refused
                interpretation = generateFallbackInterpretation(drawnCards);
            }
        }

        session.setAiInterpretation(interpretation);
        session.setStatus("COMPLETED");
        sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("aiInterpretation", interpretation);
        // 4. Legal Disclaimer attached to all interpretation
        response.put("legalDisclaimer",
                "Disclaimer: This Tarot reading is for entertainment purposes only and should not replace professional medical, legal, or financial advice.");

        return response;
    }

    private String generateFallbackInterpretation(List<ReadingCard> cards) {
        StringBuilder sb = new StringBuilder("Dựa vào những lá bài bạn đã rút, đây là thông điệp cho bạn: \n");
        for (ReadingCard card : cards) {
            sb.append("- Vị trí ").append(card.getPositionIndex()).append(": ")
                    .append(card.getCardTemplate().getName());

            if (Boolean.TRUE.equals(card.getIsReversed())) {
                sb.append(" (Ngược). ");
            } else {
                sb.append(" (Xuôi). ");
            }

            Optional<DivineHelper> helperOpt = divineHelperRepository
                    .findByCardTemplate_CardTemplateId(card.getCardTemplate().getCardTemplateId());
            if (helperOpt.isPresent()) {
                DivineHelper helper = helperOpt.get();
                if (Boolean.TRUE.equals(card.getIsReversed())) {
                    sb.append(helper.getReversedMeaning()).append("\n");
                } else {
                    sb.append(helper.getUprightMeaning()).append("\n");
                }
            } else {
                sb.append("Lá bài mang bí ẩn tự nhiên chờ bạn tự khám phá.\n");
            }
        }
        sb.append("\n(Được cung cấp bởi Cơ chế Lốp Dự Phòng Local-Fallback).");
        return sb.toString();
    }
}
