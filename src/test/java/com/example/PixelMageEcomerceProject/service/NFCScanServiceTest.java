package com.example.PixelMageEcomerceProject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.ReadingCardRepository;
import com.example.PixelMageEcomerceProject.service.impl.NFCScanServiceImpl;
import com.example.PixelMageEcomerceProject.exceptions.CardLockedInSessionException;
import com.example.PixelMageEcomerceProject.service.interfaces.AchievementService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;
import com.example.PixelMageEcomerceProject.service.interfaces.WebSocketNotificationService;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class NFCScanServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserInventoryService userInventoryService;
    @Mock
    private ReadingCardRepository readingCardRepository;
    @Mock
    private WebSocketNotificationService wsNotificationService;
    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private NFCScanServiceImpl nfcScanService;

    @Test
    void scanNFC_cardReady_returnsLinkPrompt() {
        Card card = new Card();
        card.setStatus(CardProductStatus.READY);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        Map<String, Object> result = nfcScanService.scanNFC("UID-123", 100);

        assertThat(result.get("action")).isEqualTo("LINK_PROMPT");
    }

    @Test
    void scanNFC_cardSold_returnsLinkPrompt() {
        Card card = new Card();
        card.setStatus(CardProductStatus.SOLD);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        Map<String, Object> result = nfcScanService.scanNFC("UID-123", 100);

        assertThat(result.get("action")).isEqualTo("LINK_PROMPT");
    }

    @Test
    void scanNFC_cardLinked_ownUser_returnsViewContent() {
        Card card = new Card();
        card.setStatus(CardProductStatus.LINKED);
        Account owner = new Account();
        owner.setCustomerId(100);
        card.setOwner(owner);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        Map<String, Object> result = nfcScanService.scanNFC("UID-123", 100);

        assertThat(result.get("action")).isEqualTo("VIEW_CONTENT");
    }

    @Test
    void scanNFC_cardLinked_otherUser_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.LINKED);
        Account owner = new Account();
        owner.setCustomerId(999);
        card.setOwner(owner);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> nfcScanService.scanNFC("UID-123", 100));
    }

    @Test
    void scanNFC_cardPendingBind_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.PENDING_BIND);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> nfcScanService.scanNFC("UID-123", 100));
    }

    @Test
    void linkCard_success() {
        Card card = new Card();
        card.setCardId(1);
        card.setStatus(CardProductStatus.READY);
        CardTemplate ct = new CardTemplate();
        ct.setCardTemplateId(5);
        ct.setName("The Fool");
        card.setCardTemplate(ct);

        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));
        Account account = new Account();
        account.setCustomerId(100);
        when(accountRepository.findById(100)).thenReturn(Optional.of(account));

        Map<String, Object> result = nfcScanService.linkCard("UID-123", 100);

        assertThat(result.get("message")).isEqualTo("Card linked successfully");
        assertThat(card.getStatus()).isEqualTo(CardProductStatus.LINKED);
        verify(userInventoryService).upsertInventory(100, 5, 1);
        verify(cardRepository).save(card);
    }

    @Test
    void linkCard_cardNotLinkable_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.LINKED);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> nfcScanService.linkCard("UID-123", 100));
        verify(userInventoryService, never()).upsertInventory(anyInt(), anyInt(), anyInt());
    }

    @Test
    void linkCard_cardInActiveSession_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.READY);
        CardTemplate ct = new CardTemplate();
        ct.setCardTemplateId(5);
        card.setCardTemplate(ct);

        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));
        when(readingCardRepository.existsByCardTemplate_CardTemplateIdAndReadingSession_StatusIn(
                5, Arrays.asList("PENDING", "INTERPRETING"))).thenReturn(true);

        assertThrows(CardLockedInSessionException.class, () -> nfcScanService.linkCard("UID-123", 100));
        verify(userInventoryService, never()).upsertInventory(anyInt(), anyInt(), anyInt());
    }

    @Test
    void unlinkCard_cardInActiveSession_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.LINKED);
        CardTemplate ct = new CardTemplate();
        ct.setCardTemplateId(5);
        card.setCardTemplate(ct);

        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));
        when(readingCardRepository.existsByCardTemplate_CardTemplateIdAndReadingSession_StatusIn(
                5, Arrays.asList("PENDING", "INTERPRETING"))).thenReturn(true);

        assertThrows(CardLockedInSessionException.class, () -> nfcScanService.unlinkCard("UID-123", 100));
        verify(userInventoryService, never()).upsertInventory(anyInt(), anyInt(), anyInt());
    }

    @Test
    void unlinkCard_success() {
        Card card = new Card();
        card.setCardId(1);
        card.setStatus(CardProductStatus.LINKED);
        CardTemplate ct = new CardTemplate();
        ct.setCardTemplateId(5);
        ct.setName("The Fool");
        card.setCardTemplate(ct);

        Account owner = new Account();
        owner.setCustomerId(100);
        card.setOwner(owner);

        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));
        when(readingCardRepository.existsByCardTemplate_CardTemplateIdAndReadingSession_StatusIn(
                5, Arrays.asList("PENDING", "INTERPRETING"))).thenReturn(false);

        Map<String, Object> result = nfcScanService.unlinkCard("UID-123", 100);

        assertThat(result.get("message")).isEqualTo("Card unlinked successfully");
        assertThat(card.getStatus()).isEqualTo(CardProductStatus.READY);
        assertThat(card.getOwner()).isNull();
        verify(userInventoryService).upsertInventory(100, 5, -1);
        verify(cardRepository).save(card);
    }
}
