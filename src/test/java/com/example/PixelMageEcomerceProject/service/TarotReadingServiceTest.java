package com.example.PixelMageEcomerceProject.service;

import com.example.PixelMageEcomerceProject.entity.*;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionMode;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionStatus;
import com.example.PixelMageEcomerceProject.exceptions.InsufficientCardsException;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void testDrawCards_YourDeck_Success_3Cards() {
        when(sessionRepository.findById(1)).thenReturn(Optional.of(sessionYourDeck));
        List<CardTemplate> userCards = new java.util.ArrayList<>(List.of(new CardTemplate(), new CardTemplate(), new CardTemplate()));
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
        // User has only 2 cards
        List<CardTemplate> userCards = new java.util.ArrayList<>(List.of(new CardTemplate(), new CardTemplate()));
        when(userInventoryService.getLinkedCardTemplates(1)).thenReturn(userCards);

        assertThrows(InsufficientCardsException.class, () -> tarotReadingService.drawCards(1, false));
    }

    @Test
    void testDrawCards_Explore_Success() {
        when(sessionRepository.findById(2)).thenReturn(Optional.of(sessionExplore));
        // Assume context has 78 cards, we just mock 3 for Explore pool
        List<CardTemplate> allCards = new java.util.ArrayList<>(List.of(new CardTemplate(), new CardTemplate(), new CardTemplate()));
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
        // User has only 2 cards, spread needs 3
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(2);

        assertThrows(InsufficientCardsException.class, () -> tarotReadingService.createSession(1, 1, "YOUR_DECK"));
    }
}
