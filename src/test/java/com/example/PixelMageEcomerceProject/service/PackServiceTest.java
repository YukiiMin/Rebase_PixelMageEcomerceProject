package com.example.PixelMageEcomerceProject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.PixelMageEcomerceProject.dto.request.PackRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.PackDetailRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.impl.PackServiceImpl;

@ExtendWith(MockitoExtension.class)
class PackServiceTest {

    @Mock
    private PackRepository packRepository;
    @Mock
    private PackDetailRepository packDetailRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private PackServiceImpl packService;

    @Test
    void createPack_success() {
        // Arrange
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(1);
        requestDTO.setCreatedByAccountId(100);

        Product product = new Product();
        product.setProductId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        Account account = new Account();
        account.setCustomerId(100);
        when(accountRepository.findById(100)).thenReturn(Optional.of(account));

        // Mock saving Pack
        Pack savedPack = new Pack();
        savedPack.setPackId(10);
        when(packRepository.save(any(Pack.class))).thenReturn(savedPack);

        // Mock finding ready cards (need at least 3)
        List<Card> readyCards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Card c = new Card();
            c.setCardId(i);
            c.setStatus(CardProductStatus.READY.name());
            readyCards.add(c);
        }
        when(cardRepository.findByStatus(CardProductStatus.READY.name())).thenReturn(readyCards);

        // Act
        Pack result = packService.createPack(requestDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("STOCKED");
        verify(cardRepository, times(3)).save(any(Card.class)); // 3 cards set to SOLD
        verify(packDetailRepository, times(1)).saveAll(anyList());
        verify(packRepository, times(2)).save(any(Pack.class)); // 1 for created, 1 for stocked
    }

    @Test
    void createPack_productNotFound_throwsException() {
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(99);

        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> packService.createPack(requestDTO));
        verify(packRepository, never()).save(any());
    }

    @Test
    void createPack_notEnoughReadyCards_throwsException() {
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(1);

        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));

        when(packRepository.save(any(Pack.class))).thenReturn(new Pack());

        // Only 2 cards (need 3)
        List<Card> readyCards = new ArrayList<>();
        readyCards.add(new Card());
        readyCards.add(new Card());
        when(cardRepository.findByStatus(CardProductStatus.READY.name())).thenReturn(readyCards);

        assertThrows(RuntimeException.class, () -> packService.createPack(requestDTO));
        verify(packDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void updatePackStatus_success() {
        Pack mockPack = new Pack();
        mockPack.setPackId(1);
        mockPack.setStatus("STOCKED");
        when(packRepository.findById(1)).thenReturn(Optional.of(mockPack));
        when(packRepository.save(any())).thenReturn(mockPack);

        Pack result = packService.updatePackStatus(1, "RESERVED");

        assertThat(result.getStatus()).isEqualTo("RESERVED");
        verify(packRepository, times(1)).save(mockPack);
    }

    @Test
    void updatePackStatus_packNotFound_throwsException() {
        when(packRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> packService.updatePackStatus(99, "RESERVED"));
    }
}
