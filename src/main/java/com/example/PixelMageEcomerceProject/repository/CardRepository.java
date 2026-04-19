package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.enums.CardTemplateRarity;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
    Optional<Card> findByNfcUid(String nfcUid);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Card c WHERE c.nfcUid = :nfcUid")
    Optional<Card> findLockedByNfcUid(@org.springframework.data.repository.query.Param("nfcUid") String nfcUid);

    List<Card> findByStatus(CardProductStatus status);

    List<Card> findByCardTemplate_RarityAndStatus(CardTemplateRarity rarity, CardProductStatus status);

    long countByCardTemplate_CardTemplateIdAndStatus(Integer cardTemplateId, CardProductStatus status);
}
