package com.example.PixelMageEcomerceProject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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
import com.example.PixelMageEcomerceProject.entity.PackDetail;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.PackDetailRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.impl.PackServiceImpl;
import com.example.PixelMageEcomerceProject.exceptions.InsufficientCardsException;

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
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(1);
        requestDTO.setCreatedByAccountId(100);

        Product product = new Product();
        product.setProductId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        Account account = new Account();
        account.setCustomerId(100);
        when(accountRepository.findById(100)).thenReturn(Optional.of(account));

        Pack savedPack = new Pack();
        savedPack.setPackId(10);
        when(packRepository.save(any(Pack.class))).thenReturn(savedPack);

        List<Card> commonCards = new ArrayList<>();
        commonCards.add(new Card());
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.COMMON), eq(CardProductStatus.READY)))
            .thenReturn(commonCards);
        
        List<Card> rareCards = new ArrayList<>();
        rareCards.add(new Card());
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.RARE), eq(CardProductStatus.READY)))
            .thenReturn(rareCards);
            
        List<Card> legendaryCards = new ArrayList<>();
        legendaryCards.add(new Card());
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.LEGENDARY), eq(CardProductStatus.READY)))
            .thenReturn(legendaryCards);

        Pack result = packService.createPack(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PackStatus.STOCKED);
        verify(cardRepository, never()).save(any(Card.class)); // cards should NOT be saved
        verify(packDetailRepository, times(1)).saveAll(anyList());
        verify(packRepository, times(2)).save(any(Pack.class)); // 1 for entity, 1 for update
    }

    @Test
    void createPack_notEnoughReadyCards_throwsException() {
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(1);

        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));
        when(packRepository.save(any(Pack.class))).thenReturn(new Pack());

        // Pool completely empty
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(any(), eq(CardProductStatus.READY)))
            .thenReturn(new ArrayList<>());

        assertThrows(InsufficientCardsException.class, () -> packService.createPack(requestDTO));
        verify(packDetailRepository, never()).saveAll(anyList());
    }

    @Test
    void createPack_dropRate_slot5_100runs() {
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));
        when(packRepository.save(any(Pack.class))).thenAnswer(i -> {
            Pack p = i.getArgument(0);
            if(p.getPackId() == null) p.setPackId(1);
            return p;
        });

        List<Card> commonCards = new ArrayList<>();
        Card cCommon = new Card(); cCommon.setCardId(1);
        commonCards.add(cCommon);
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.COMMON), eq(CardProductStatus.READY)))
            .thenReturn(commonCards);
        
        List<Card> rareCards = new ArrayList<>();
        Card cRare = new Card(); cRare.setCardId(2);
        rareCards.add(cRare);
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.RARE), eq(CardProductStatus.READY)))
            .thenReturn(rareCards);

        List<Card> legendaryCards = new ArrayList<>();
        Card cLeg = new Card(); cLeg.setCardId(3);
        legendaryCards.add(cLeg);
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.LEGENDARY), eq(CardProductStatus.READY)))
            .thenReturn(legendaryCards);

        int legendaryCount = 0;
        for(int i = 0; i < 100; i++) {
            Pack result = packService.createPack(requestDTO);
            List<PackDetail> details = result.getPackDetails();
            if (details.get(4).getCard() != null && details.get(4).getCard().getCardId() != null && details.get(4).getCard().getCardId() == 3) {
                legendaryCount++;
            }
        }
        
        System.out.println("Slot 5 Legendary count: " + legendaryCount);
        assertTrue(legendaryCount >= 10 && legendaryCount <= 30, "Legendary count " + legendaryCount + " not in 10-30 tolerance");
    }

    @Test
    void createPack_dropRate_slot4_100runs() {
        PackRequestDTO requestDTO = new PackRequestDTO();
        requestDTO.setProductId(1);
        when(productRepository.findById(1)).thenReturn(Optional.of(new Product()));
        when(packRepository.save(any(Pack.class))).thenAnswer(i -> {
            Pack p = i.getArgument(0);
            if(p.getPackId() == null) p.setPackId(1);
            return p;
        });

        List<Card> commonCards = new ArrayList<>();
        Card cCommon = new Card(); cCommon.setCardId(1);
        commonCards.add(cCommon);
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.COMMON), eq(CardProductStatus.READY)))
            .thenReturn(commonCards);
        
        List<Card> rareCards = new ArrayList<>();
        Card cRare = new Card(); cRare.setCardId(2);
        rareCards.add(cRare);
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.RARE), eq(CardProductStatus.READY)))
            .thenReturn(rareCards);

        List<Card> legendaryCards = new ArrayList<>();
        Card cLeg = new Card(); cLeg.setCardId(3);
        legendaryCards.add(cLeg);
        lenient().when(cardRepository.findByCardTemplate_RarityAndStatus(eq(CardTemplateRarity.LEGENDARY), eq(CardProductStatus.READY)))
            .thenReturn(legendaryCards);

        int rareCount = 0;
        for(int i = 0; i < 100; i++) {
            Pack result = packService.createPack(requestDTO);
            List<PackDetail> details = result.getPackDetails();
            if (details.get(3).getCard() != null && details.get(3).getCard().getCardId() != null && details.get(3).getCard().getCardId() == 2) {
                rareCount++;
            }
        }
        
        System.out.println("Slot 4 Rare count: " + rareCount);
        assertTrue(rareCount >= 20 && rareCount <= 40, "Rare count " + rareCount + " not in 20-40 tolerance");
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
    void updatePackStatus_success() {
        Pack mockPack = new Pack();
        mockPack.setPackId(1);
        mockPack.setStatus(PackStatus.STOCKED);
        when(packRepository.findById(1)).thenReturn(Optional.of(mockPack));
        when(packRepository.save(any())).thenReturn(mockPack);

        Pack result = packService.updatePackStatus(1, "RESERVED");

        assertThat(result.getStatus()).isEqualTo(PackStatus.RESERVED);
        verify(packRepository, times(1)).save(mockPack);
    }

    @Test
    void updatePackStatus_packNotFound_throwsException() {
        when(packRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> packService.updatePackStatus(99, "RESERVED"));
    }
}
