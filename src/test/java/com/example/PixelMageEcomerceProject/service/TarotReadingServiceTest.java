package com.example.PixelMageEcomerceProject.service;

import com.example.PixelMageEcomerceProject.entity.*;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionMode;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionStatus;
import com.example.PixelMageEcomerceProject.exceptions.ActiveSessionExistsException;
import com.example.PixelMageEcomerceProject.exceptions.InsufficientCardsException;
import com.example.PixelMageEcomerceProject.exceptions.RedisUnavailableException;
import com.example.PixelMageEcomerceProject.exceptions.SessionExpiredException;
import com.example.PixelMageEcomerceProject.repository.*;
import com.example.PixelMageEcomerceProject.service.interfaces.CardTemplateService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;
import com.example.PixelMageEcomerceProject.service.impl.TarotReadingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TarotReadingServiceTest {

    @Mock
    private ReadingSessionRepository sessionRepository;

    @Mock
    private ReadingCardRepository readingCardRepository;

    @Mock
    private CardTemplateService cardTemplateService;

    @Mock
    private UserInventoryService userInventoryService;

    @Mock
    private SpreadRepository spreadRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TarotReadingServiceImpl tarotReadingService;

    private ReadingSession sessionYourDeck;
    private ReadingSession sessionExplore;
    private Spread spread3Cards;
    private Spread spread10Cards;
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setCustomerId(1);

        spread3Cards = new Spread();
        spread3Cards.setSpreadId(1);
        spread3Cards.setPositionCount(3);
        spread3Cards.setMinCardsRequired(3);

        spread10Cards = new Spread();
        spread10Cards.setSpreadId(2);
        spread10Cards.setPositionCount(10);
        spread10Cards.setMinCardsRequired(10);

        sessionYourDeck = new ReadingSession();
        sessionYourDeck.setSessionId(1);
        sessionYourDeck.setAccount(account);
        sessionYourDeck.setSpread(spread3Cards);
        sessionYourDeck.setMode(ReadingSessionMode.YOUR_DECK);
        sessionYourDeck.setStatus(ReadingSessionStatus.PENDING);

        sessionExplore = new ReadingSession();
        sessionExplore.setSessionId(2);
        sessionExplore.setAccount(account);
        sessionExplore.setSpread(spread3Cards);
        sessionExplore.setMode(ReadingSessionMode.EXPLORE);
        sessionExplore.setStatus(ReadingSessionStatus.PENDING);
    }

    // ── Pre-existing tests (unchanged) ────────────────────────────────────────

    @Test
    void testDrawCards_YourDeck_Success_3Cards() {
        when(sessionRepository.findById(1)).thenReturn(Optional.of(sessionYourDeck));
        List<CardTemplate> userCards = new ArrayList<>(List.of(new CardTemplate(), new CardTemplate(), new CardTemplate()));
        when(userInventoryService.getLinkedCardTemplates(1)).thenReturn(userCards);
        when(readingCardRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Map<String, Object> result = tarotReadingService.drawCards(1, false);

        assertNotNull(result);
        
        List<Map<String, Object>> drawnCards = (List<Map<String, Object>>) result.get("drawnCards");
        assertEquals(3, drawnCards.size());
        verify(userInventoryService).getLinkedCardTemplates(1);
    }

    @Test
    void testDrawCards_YourDeck_Fail_NotEnoughCards() {
        when(sessionRepository.findById(1)).thenReturn(Optional.of(sessionYourDeck));
        List<CardTemplate> userCards = new ArrayList<>(List.of(new CardTemplate(), new CardTemplate()));
        when(userInventoryService.getLinkedCardTemplates(1)).thenReturn(userCards);

        assertThrows(InsufficientCardsException.class, () -> tarotReadingService.drawCards(1, false));
    }

    @Test
    void testDrawCards_Explore_Success() {
        when(sessionRepository.findById(2)).thenReturn(Optional.of(sessionExplore));
        // Redis key exists — session not expired
        when(redisTemplate.hasKey("explore:session:2")).thenReturn(true);

        List<CardTemplate> allCards = new ArrayList<>(List.of(new CardTemplate(), new CardTemplate(), new CardTemplate()));
        when(cardTemplateService.getAllCardTemplates()).thenReturn(allCards);
        when(readingCardRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Map<String, Object> result = tarotReadingService.drawCards(2, false);

        assertNotNull(result);
        verify(cardTemplateService).getAllCardTemplates();
    }

    @Test
    void testCreateSession_YourDeck_MinCardsValidation() {
        when(spreadRepository.findById(1)).thenReturn(Optional.of(spread3Cards));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(2);
        // No active session
        when(sessionRepository.findFirstByAccount_CustomerIdAndStatusIn(eq(1), anyList()))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientCardsException.class, () -> tarotReadingService.createSession(1, 1, "YOUR_DECK"));
    }

    // ── TASK-01 tests — 4 done conditions ────────────────────────────────────

    /**
     * Done condition 1: User has active session → create second session → 409 with activeSessionId in response body
     */
    @Test
    void createSession_activeSession_throws409WithActiveSessionId() {
        // Given: an existing PENDING session
        ReadingSession activeSession = new ReadingSession();
        activeSession.setSessionId(99);
        activeSession.setStatus(ReadingSessionStatus.PENDING);

        when(spreadRepository.findById(1)).thenReturn(Optional.of(spread3Cards));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(sessionRepository.findFirstByAccount_CustomerIdAndStatusIn(eq(1), anyList()))
                .thenReturn(Optional.of(activeSession));

        // When / Then
        ActiveSessionExistsException ex = assertThrows(
                ActiveSessionExistsException.class,
                () -> tarotReadingService.createSession(1, 1, "YOUR_DECK")
        );
        assertEquals(99, ex.getActiveSessionId());
        assertEquals("Bạn đang có một phiên đọc bài chưa hoàn thành.", ex.getMessage());
    }

    /**
     * Done condition 2: EXPLORE session created → Redis key set with TTL ≤ 1800s (30 min)
     */
    @Test
    void createSession_explore_redisKeySetWith30MinTTL() {
        // No active session
        when(spreadRepository.findById(1)).thenReturn(Optional.of(spread3Cards));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(sessionRepository.findFirstByAccount_CustomerIdAndStatusIn(eq(1), anyList()))
                .thenReturn(Optional.empty());
        // User has cards — skip guest limit
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(5);

        // Saved session with EXPLORE mode
        ReadingSession saved = new ReadingSession();
        saved.setSessionId(10);
        saved.setMode(ReadingSessionMode.EXPLORE);
        saved.setStatus(ReadingSessionStatus.PENDING);
        when(sessionRepository.save(any())).thenReturn(saved);

        // Mock ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tarotReadingService.createSession(1, 1, "EXPLORE");

        // Verify Redis key set with exactly 30 minutes TTL
        verify(valueOperations).set(
                eq("explore:session:10"),
                eq("active"),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    /**
     * Done condition 3 (fail-closed): Redis unavailable → 503 (no session created)
     */
    @Test
    void createSession_explore_redisDown_throws503() {
        when(spreadRepository.findById(1)).thenReturn(Optional.of(spread3Cards));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(sessionRepository.findFirstByAccount_CustomerIdAndStatusIn(eq(1), anyList()))
                .thenReturn(Optional.empty());
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(5);

        ReadingSession saved = new ReadingSession();
        saved.setSessionId(10);
        saved.setMode(ReadingSessionMode.EXPLORE);
        saved.setStatus(ReadingSessionStatus.PENDING);
        when(sessionRepository.save(any())).thenReturn(saved);

        // Redis throws on set
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Connection refused"))
                .when(valueOperations).set(anyString(), any(), anyLong(), any());

        // Must throw RedisUnavailableException (503)
        assertThrows(
                RedisUnavailableException.class,
                () -> tarotReadingService.createSession(1, 1, "EXPLORE")
        );
    }

    /**
     * Done condition 4: After 30 min (mock TTL expiry) → session status = EXPIRED
     */
    @Test
    void drawCards_explore_ttlExpired_sessionSetToExpired() {
        // EXPLORE PENDING session
        sessionExplore.setMode(ReadingSessionMode.EXPLORE);
        sessionExplore.setStatus(ReadingSessionStatus.PENDING);
        when(sessionRepository.findById(2)).thenReturn(Optional.of(sessionExplore));

        // Redis key is gone (TTL expired)
        when(redisTemplate.hasKey("explore:session:2")).thenReturn(false);

        // Must throw SessionExpiredException (410)
        SessionExpiredException ex = assertThrows(
                SessionExpiredException.class,
                () -> tarotReadingService.drawCards(2, false)
        );
        assertTrue(ex.getMessage().contains("hết hạn"));

        // Session status must be EXPIRED and saved
        assertEquals(ReadingSessionStatus.EXPIRED, sessionExplore.getStatus());
        verify(sessionRepository).save(sessionExplore);
    }
}
