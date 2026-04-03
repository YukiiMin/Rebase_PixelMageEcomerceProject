package com.example.PixelMageEcomerceProject.service.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.PixelMageEcomerceProject.dto.response.SpreadResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.DivineHelper;
import com.example.PixelMageEcomerceProject.entity.ReadingCard;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.entity.Spread;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionMode;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionStatus;
import com.example.PixelMageEcomerceProject.exceptions.ActiveSessionExistsException;
import com.example.PixelMageEcomerceProject.exceptions.GuestReadingLimitException;
import com.example.PixelMageEcomerceProject.exceptions.InsufficientCardsException;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.exceptions.SessionExpiredException;
import com.example.PixelMageEcomerceProject.mapper.SpreadMapper;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.DivineHelperRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingCardRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingSessionRepository;
import com.example.PixelMageEcomerceProject.repository.SpreadRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;
import com.example.PixelMageEcomerceProject.service.interfaces.TarotReadingService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import com.example.PixelMageEcomerceProject.dto.event.NotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarotReadingServiceImpl implements TarotReadingService {

    // All dependencies final — constructor-injected by Lombok @RequiredArgsConstructor
    private final RedisTemplate<String, Object> redisTemplate;
    private final SpreadRepository spreadRepository;
    private final ReadingSessionRepository sessionRepository;
    private final ReadingCardRepository readingCardRepository;
    private final DivineHelperRepository divineHelperRepository;
    private final AccountRepository accountRepository;
    private final CardTemplateService cardTemplateService;
    private final UserInventoryService userInventoryService;
    private final SpreadMapper spreadMapper;
    private final WebSocketNotificationService wsNotificationService;

    @Value("${OPENAI_API_KEY:}")
    private String openAiApiKey;

    @Value("${N8N_WEBHOOK_URL:http://localhost:5678/webhook/tarot-reading}")
    private String n8nWebhookUrl;

    private final SecureRandom secureRandom = new SecureRandom();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final int EXPLORE_TTL_MINUTES = 30;
    private static final String EXPLORE_REDIS_KEY_PREFIX = "explore:session:";

    @Override
    @Cacheable("spreads")
    public List<SpreadResponse> getAllSpreads() {
        return spreadMapper.toResponses(spreadRepository.findAll());
    }

    @Override
    @Transactional
    public Map<String, Object> createSession(Integer accountId, Integer spreadId, String mode) {
        // Validate Spread
        Spread spread = spreadRepository.findById(spreadId)
                .orElseThrow(() -> new RuntimeException("Spread not found"));

        // Ensure Account exists
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // ── TASK-01: One active session check ──────────────────────────────────
        Optional<ReadingSession> active = sessionRepository
                .findFirstByAccount_CustomerIdAndStatusIn(
                        accountId, List.of(ReadingSessionStatus.PENDING, ReadingSessionStatus.INTERPRETING));
        if (active.isPresent()) {
            throw new ActiveSessionExistsException(active.get().getSessionId());
        }
        // ───────────────────────────────────────────────────────────────────────

        if ("YOUR_DECK".equals(mode)) {
            int userCount = userInventoryService.getLinkedCardCount(accountId);
            if (userCount < spread.getMinCardsRequired()) {
                throw new InsufficientCardsException(
                        "Cần ít nhất " + spread.getMinCardsRequired() + " lá. Bạn đang có " + userCount);
            }
        } else {
            // EXPLORE mode guest reading limit
            boolean hasLinkedCards = userInventoryService.getLinkedCardCount(accountId) > 0;
            if (!hasLinkedCards) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime usedAt = account.getGuestReadingUsedAt();
                boolean usedToday = usedAt != null && usedAt.toLocalDate().equals(now.toLocalDate());

                if (usedToday) {
                    throw new GuestReadingLimitException(
                        "Bạn đã dùng lượt đọc thử hôm nay. " +
                        "Quay lại sau 00:00 hoặc mua Pack để đọc không giới hạn.");
                }

                account.setGuestReadingUsedAt(now);
                accountRepository.save(account);
            }
        }

        ReadingSession session = new ReadingSession();
        session.setAccount(account);
        session.setSpread(spread);
        session.setMode(mode == null ? ReadingSessionMode.EXPLORE : ReadingSessionMode.valueOf(mode));
        session.setStatus(ReadingSessionStatus.PENDING);
        ReadingSession savedSession = sessionRepository.save(session);

        // ── TASK-01: EXPLORE Redis TTL — fail-closed ───────────────────────────
        if (ReadingSessionMode.EXPLORE.equals(savedSession.getMode())) {
            String redisKey = EXPLORE_REDIS_KEY_PREFIX + savedSession.getSessionId();
            try {
                redisTemplate.opsForValue().set(redisKey, "active", EXPLORE_TTL_MINUTES, TimeUnit.MINUTES);
                log.info("[EXPLORE-TTL] Key set: {} TTL={}min", redisKey, EXPLORE_TTL_MINUTES);
            } catch (Exception e) {
                log.error("[EXPLORE-TTL] Redis unavailable — aborting session creation: {}", e.getMessage());
                // @Transactional rolls back sessionRepository.save() automatically
                throw new RedisUnavailableException(
                        "Hệ thống tạm thời không khả dụng. Vui lòng thử lại sau.");
            }
        }
        // ───────────────────────────────────────────────────────────────────────

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", savedSession.getSessionId());
        response.put("status", savedSession.getStatus());
        response.put("spreadName", spread.getName());

        // Broadcast đến admin.notifications — non-fatal
        wsNotificationService.pushToTopic("admin.notifications",
                NotificationEvent.tarotSessionStarted(
                        accountId,
                        savedSession.getSessionId(),
                        savedSession.getMode().name(),
                        spread.getName()));

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> drawCards(Integer sessionId, boolean allowReversed) {
        ReadingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // ── TASK-01: Lazy EXPIRED check for EXPLORE sessions ───────────────────
        if (ReadingSessionMode.EXPLORE.equals(session.getMode())
                && ReadingSessionStatus.PENDING.equals(session.getStatus())) {
            String redisKey = EXPLORE_REDIS_KEY_PREFIX + sessionId;
            Boolean keyExists = redisTemplate.hasKey(redisKey);
            if (!Boolean.TRUE.equals(keyExists)) {
                session.setStatus(ReadingSessionStatus.EXPIRED);
                sessionRepository.save(session);
                log.warn("[EXPLORE-TTL] Session #{} expired — key {} not found in Redis", sessionId, redisKey);
                throw new SessionExpiredException(sessionId);
            }
        }
        // ───────────────────────────────────────────────────────────────────────

        if (!ReadingSessionStatus.PENDING.equals(session.getStatus())) {
            throw new RuntimeException("Session is already completed or processing. Redraw is not allowed.");
        }

        Spread spread = session.getSpread();
        int cardsToDraw = spread.getPositionCount();

        List<CardTemplate> allCards;
        if ("YOUR_DECK".equals(session.getMode().toString())) {
            allCards = userInventoryService.getLinkedCardTemplates(session.getAccount().getCustomerId());
            if (allCards.size() < cardsToDraw) {
                throw new InsufficientCardsException(
                        "YOUR_DECK: cần " + cardsToDraw + " lá, user chỉ có " + allCards.size());
            }
        } else {
            allCards = cardTemplateService.getAllCardTemplates();
            if (allCards.size() < cardsToDraw) {
                throw new RuntimeException("Not enough cards in database to draw.");
            }
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
            cardMap.put("readingCardId", savedCard.getReadingCardId());
            cardMap.put("positionIndex", savedCard.getPositionIndex());
            cardMap.put("positionName", savedCard.getPositionName() != null ? savedCard.getPositionName() : "Vị trí " + savedCard.getPositionIndex());
            cardMap.put("isReversed", isReversed);

            Map<String, Object> templateMap = new HashMap<>();
            templateMap.put("cardTemplateId", chosenCard.getCardTemplateId());
            templateMap.put("name", chosenCard.getName());
            templateMap.put("imageUrl", chosenCard.getImagePath() != null ? chosenCard.getImagePath() : chosenCard.getDesignPath());
            templateMap.put("rarity", chosenCard.getRarity() != null ? chosenCard.getRarity().name() : "COMMON");

            cardMap.put("cardTemplate", templateMap);

            drawnCardsOutput.add(cardMap);
        }

        session.setStatus(ReadingSessionStatus.INTERPRETING);
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

                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(n8nWebhookUrl, payload, Map.class);
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
        session.setStatus(ReadingSessionStatus.COMPLETED);
        sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("aiInterpretation", interpretation);
        // 4. Legal Disclaimer attached to all interpretation
        response.put("legalDisclaimer",
                "Disclaimer: This Tarot reading is for entertainment purposes only and should not replace professional medical, legal, or financial advice.");

        // Broadcast hoàn thành phiên Tarot đến admin.notifications — non-fatal
        wsNotificationService.pushToTopic("admin.notifications",
                NotificationEvent.tarotSessionCompleted(
                        session.getAccount().getCustomerId(),
                        sessionId,
                        session.getMode().name()));

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

    @Override
    public List<ReadingSession> getSessionsByAccount(Integer accountId) {
        return sessionRepository.findByAccount_CustomerIdAndMode(accountId,
                ReadingSessionMode.YOUR_DECK);
    }
}
