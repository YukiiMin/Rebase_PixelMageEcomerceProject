package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.CollectionItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.CollectionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.AdminCollectionRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.CardCollection;
import com.example.PixelMageEcomerceProject.entity.CollectionItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface CollectionService {

    // Collection CRUD (user-owned)
    CardCollection createCollection(Integer customerId, CollectionRequestDTO request);
    CardCollection updateCollection(Integer customerId, Integer collectionId, CollectionRequestDTO request);
    void deleteCollection(Integer customerId, Integer collectionId);
    Optional<CardCollection> getCollectionById(Integer collectionId);
    List<CardCollection> getCollectionsByCustomerId(Integer customerId);
    List<CardCollection> getPublicCollections();

    // Admin-controlled collections
    CardCollection createAdminCollection(Integer adminId, AdminCollectionRequestDTO request);
    CardCollection updateCollectionVisibility(Integer collectionId, Boolean isVisible);

    // Collection Items
    CollectionItem addCardToCollection(Integer customerId, CollectionItemRequestDTO request);
    void removeCardFromCollection(Integer customerId, Integer collectionId, Integer cardId);
    List<CollectionItem> getCollectionItems(Integer collectionId);

    // Owned cards (purchased cards that can be added to collection)
    List<Card> getOwnedCards(Integer customerId);
    boolean isCardOwnedByCustomer(Integer customerId, Integer cardId);
}
