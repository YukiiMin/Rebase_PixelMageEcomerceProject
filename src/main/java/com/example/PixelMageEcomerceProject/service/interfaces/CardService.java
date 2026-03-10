package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.PixelMageEcomerceProject.dto.request.CardRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Card;

@Service
public interface CardService {
    Card createCardProduct(CardRequestDTO cardRequestDTO);

    Card bindNFC(Integer cardId, String nfcUid);

    Card updateStatus(Integer cardId, String newStatus);

    Card updateCard(Integer id, CardRequestDTO cardRequestDTO);

    void deleteCard(Integer id);

    Optional<Card> getCardById(Integer id);

    List<Card> getAllCards();

    Optional<Card> getCardByNfcUid(String nfcUid);
}
