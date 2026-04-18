package com.example.PixelMageEcomerceProject.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.entity.Spread;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionMode;
import com.example.PixelMageEcomerceProject.exceptions.GuestReadingLimitException;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingSessionRepository;
import com.example.PixelMageEcomerceProject.repository.SpreadRepository;
import com.example.PixelMageEcomerceProject.service.impl.TarotReadingServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;

class GuestReadingLimitTest {

    @Mock
    private SpreadRepository spreadRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ReadingSessionRepository sessionRepository;

    @Mock
    private UserInventoryService userInventoryService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TarotReadingServiceImpl tarotReadingService;

    private Account guestAccount;
    private Spread spread;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        guestAccount = new Account();
        guestAccount.setCustomerId(1);
        
        spread = new Spread();
        spread.setSpreadId(1);
        spread.setName("One Card");
        spread.setMinCardsRequired(1);

        when(spreadRepository.findById(anyInt())).thenReturn(Optional.of(spread));
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(guestAccount));
        when(sessionRepository.save(any(ReadingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock redis opsForValue for EXPLORE session creation TTL set
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGuestExplore_FirstTimeToday_Success() {
        // Condition: No linked cards, guestReadingUsedAt is null
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(0);
        guestAccount.setGuestReadingUsedAt(null);

        assertDoesNotThrow(() -> tarotReadingService.createSession(1, 1, "EXPLORE", "My question"));
        
        verify(accountRepository, times(1)).save(guestAccount);
        assertNotNull(guestAccount.getGuestReadingUsedAt());
    }

    @Test
    void testGuestExplore_SecondTimeToday_ThrowsException() {
        // Condition: No linked cards, already used today
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(0);
        guestAccount.setGuestReadingUsedAt(LocalDateTime.now());

        assertThrows(GuestReadingLimitException.class, () -> 
            tarotReadingService.createSession(1, 1, "EXPLORE", "My question")
        );
    }

    @Test
    void testGuestExplore_NextDay_Success() {
        // Condition: No linked cards, used yesterday
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(0);
        guestAccount.setGuestReadingUsedAt(LocalDateTime.now().minusDays(1));

        assertDoesNotThrow(() -> tarotReadingService.createSession(1, 1, "EXPLORE", "My question"));
        verify(accountRepository, times(1)).save(guestAccount);
    }

    @Test
    void testUserWithLinkedCards_ExploreMultipleTimes_Success() {
        // Condition: Has linked cards
        when(userInventoryService.getLinkedCardCount(1)).thenReturn(5);
        guestAccount.setGuestReadingUsedAt(LocalDateTime.now());

        // Should not throw even if guestReadingUsedAt was set today (though it shouldn't be affected)
        assertDoesNotThrow(() -> tarotReadingService.createSession(1, 1, "EXPLORE", "My question"));
        assertDoesNotThrow(() -> tarotReadingService.createSession(1, 1, "EXPLORE", "My question"));
    }

    @Test
    void testGetSessions_ReturnsOnlyYourDeck() {
        // Condition: Filter explore sessions
        tarotReadingService.getSessionsByAccount(1);
        verify(sessionRepository).findByAccount_CustomerIdAndMode(1, ReadingSessionMode.YOUR_DECK);
    }
}
