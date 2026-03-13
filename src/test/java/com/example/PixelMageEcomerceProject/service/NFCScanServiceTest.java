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
import com.example.PixelMageEcomerceProject.service.impl.NFCScanServiceImpl;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;

@ExtendWith(MockitoExtension.class)
class NFCScanServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserInventoryService userInventoryService;

    @InjectMocks
    private NFCScanServiceImpl nfcScanService;

    @Test
    void scanNFC_cardReady_returnsLinkPrompt() {
        Card card = new Card();
        card.setStatus(CardProductStatus.READY.name());
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        Map<String, Object> result = nfcScanService.scanNFC("UID-123", 100);

        assertThat(result.get("action")).isEqualTo("LINK_PROMPT");
    }

    @Test
    void scanNFC_cardSold_returnsLinkPrompt() {
        Card card = new Card();
        card.setStatus(CardProductStatus.SOLD.name());
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        Map<String, Object> result = nfcScanService.scanNFC("UID-123", 100);

        assertThat(result.get("action")).isEqualTo("LINK_PROMPT");
    }

    @Test
    void scanNFC_cardLinked_ownUser_returnsViewContent() {
        Card card = new Card();
        card.setStatus(CardProductStatus.LINKED.name());
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
        card.setStatus(CardProductStatus.LINKED.name());
        Account owner = new Account();
        owner.setCustomerId(999);
        card.setOwner(owner);
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> nfcScanService.scanNFC("UID-123", 100));
    }

    @Test
    void scanNFC_cardPendingBind_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.PENDING_BIND.name());
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> nfcScanService.scanNFC("UID-123", 100));
    }

    @Test
    void linkCard_success() {
        Card card = new Card();
        card.setStatus(CardProductStatus.READY.name());
        CardTemplate ct = new CardTemplate();
        ct.setCardTemplateId(5);
        card.setCardTemplate(ct);

        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));
        Account account = new Account();
        account.setCustomerId(100);
        when(accountRepository.findById(100)).thenReturn(Optional.of(account));

        Map<String, Object> result = nfcScanService.linkCard("UID-123", 100);

        assertThat(result.get("message")).isEqualTo("Card linked successfully");
        assertThat(card.getStatus()).isEqualTo(CardProductStatus.LINKED.name());
        verify(userInventoryService).upsertInventory(100, 5, 1);
        verify(cardRepository).save(card);
    }

    @Test
    void linkCard_cardNotLinkable_throwsException() {
        Card card = new Card();
        card.setStatus(CardProductStatus.LINKED.name());
        when(cardRepository.findByNfcUid("UID-123")).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> nfcScanService.linkCard("UID-123", 100));
        verify(userInventoryService, never()).upsertInventory(anyInt(), anyInt(), anyInt());
    }
}
