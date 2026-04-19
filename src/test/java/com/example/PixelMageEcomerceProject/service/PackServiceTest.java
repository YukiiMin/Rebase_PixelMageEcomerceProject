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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.PixelMageEcomerceProject.dto.request.PackRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PackResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.entity.PackCategory;
import com.example.PixelMageEcomerceProject.entity.PackDetail;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.mapper.PackMapper;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.PackCategoryRepository;
import com.example.PixelMageEcomerceProject.repository.PackDetailRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.impl.PackServiceImpl;

@ExtendWith(MockitoExtension.class)
class PackServiceTest {

    @Mock private PackRepository packRepository;
    @Mock private PackDetailRepository packDetailRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CardRepository cardRepository;
    @Mock private PackCategoryRepository packCategoryRepository;
    @Mock private PackMapper packMapper;

    @InjectMocks
    private PackServiceImpl packService;

    // ── Shared test fixture ──────────────────────────────────────────────────
    private PackCategory buildCategory(int cardsPerPack, String rarityRates) {
        PackCategory cat = new PackCategory();
        cat.setPackCategoryId(1);
        cat.setName("Test Category");
        cat.setCardsPerPack(cardsPerPack);
        cat.setRarityRates(rarityRates);

        // Build a minimal card pool: 1 COMMON, 1 RARE, 1 LEGENDARY
        List<CardTemplate> pool = new ArrayList<>();
        for (CardTemplateRarity rarity : CardTemplateRarity.values()) {
            CardTemplate ct = new CardTemplate();
            ct.setCardTemplateId(rarity.ordinal() + 1);
            ct.setName(rarity.name() + "_Template");
            ct.setRarity(rarity);
            pool.add(ct);
        }
        cat.setCardPools(pool);
        return cat;
    }

    private Pack buildSavedPack(int packId) {
        Pack p = new Pack();
        p.setPackId(packId);
        p.setStatus(PackStatus.STOCKED);
        p.setPackDetails(new ArrayList<>());
        return p;
    }

    // ── generatePacks — success ──────────────────────────────────────────────
    @Test
    void generatePacks_success_returnsCorrectCount() {
        // Arrange
        PackCategory cat = buildCategory(5, "{\"COMMON\":60,\"RARE\":30,\"LEGENDARY\":10}");
        when(packCategoryRepository.findById(1)).thenReturn(Optional.of(cat));

        int callCount[] = {0};
        when(packRepository.save(any(Pack.class))).thenAnswer(inv -> {
            Pack p = inv.getArgument(0);
            if (p.getPackId() == null) p.setPackId(++callCount[0]);
            return p;
        });
        when(packDetailRepository.save(any(PackDetail.class))).thenAnswer(inv -> inv.getArgument(0));
        when(packMapper.toResponse(any(Pack.class))).thenReturn(PackResponse.builder().status(PackStatus.STOCKED).build());

        // Act
        List<PackResponse> result = packService.generatePacks(1, 3);

        // Assert
        assertThat(result).hasSize(3);
        result.forEach(r -> assertThat(r.getStatus()).isEqualTo(PackStatus.STOCKED));
        // 3 packs × 5 cards = 15 PackDetail saves
        verify(packDetailRepository, times(15)).save(any(PackDetail.class));
        verify(packRepository, times(3)).save(any(Pack.class));
    }

    // ── generatePacks — PackCategory not found ───────────────────────────────
    @Test
    void generatePacks_categoryNotFound_throwsException() {
        when(packCategoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> packService.generatePacks(99, 5));
        verify(packRepository, never()).save(any());
    }

    // ── generatePacks — empty card pool ─────────────────────────────────────
    @Test
    void generatePacks_emptyCardPool_throwsException() {
        PackCategory cat = new PackCategory();
        cat.setPackCategoryId(1);
        cat.setCardsPerPack(5);
        cat.setRarityRates("{\"COMMON\":100}");
        cat.setCardPools(new ArrayList<>()); // empty pool

        when(packCategoryRepository.findById(1)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class,
                () -> packService.generatePacks(1, 1));
        verify(packDetailRepository, never()).save(any());
    }

    // ── generatePacks — invalid rarityRates JSON ─────────────────────────────
    @Test
    void generatePacks_invalidRarityRatesJson_throwsException() {
        PackCategory cat = buildCategory(5, "NOT_VALID_JSON");
        when(packCategoryRepository.findById(1)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class,
                () -> packService.generatePacks(1, 1));
        verify(packRepository, never()).save(any());
    }

    // ── generatePacks — generates exactly cardsPerPack details per pack ──────
    @Test
    void generatePacks_eachPackHasCorrectCardCount() {
        int cardsPerPack = 3;
        PackCategory cat = buildCategory(cardsPerPack, "{\"COMMON\":70,\"RARE\":25,\"LEGENDARY\":5}");
        when(packCategoryRepository.findById(1)).thenReturn(Optional.of(cat));

        int[] counter = {0};
        when(packRepository.save(any(Pack.class))).thenAnswer(inv -> {
            Pack p = inv.getArgument(0);
            if (p.getPackId() == null) p.setPackId(++counter[0]);
            return p;
        });
        when(packDetailRepository.save(any(PackDetail.class))).thenAnswer(inv -> inv.getArgument(0));
        when(packMapper.toResponse(any(Pack.class))).thenReturn(new PackResponse());

        int quantity = 4;
        packService.generatePacks(1, quantity);

        // quantity packs × cardsPerPack details each
        verify(packDetailRepository, times(quantity * cardsPerPack)).save(any(PackDetail.class));
    }

    // ── updatePackStatus — success ───────────────────────────────────────────
    @Test
    void updatePackStatus_success() {
        Pack mockPack = new Pack();
        mockPack.setPackId(1);
        mockPack.setStatus(PackStatus.STOCKED);
        when(packRepository.findById(1)).thenReturn(Optional.of(mockPack));
        when(packRepository.save(any())).thenReturn(mockPack);
        when(packMapper.toResponse(any(Pack.class)))
                .thenReturn(PackResponse.builder().status(PackStatus.RESERVED).build());

        PackResponse result = packService.updatePackStatus(1, PackStatus.RESERVED);

        assertThat(result.getStatus()).isEqualTo(PackStatus.RESERVED);
        verify(packRepository, times(1)).save(mockPack);
    }

    // ── updatePackStatus — not found ─────────────────────────────────────────
    @Test
    void updatePackStatus_packNotFound_throwsException() {
        when(packRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> packService.updatePackStatus(99, PackStatus.RESERVED));
    }
}
