package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.CollectionItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.CollectionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.AdminCollectionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.CollectionResponse;
import com.example.PixelMageEcomerceProject.dto.response.CollectionItemResponse;
import com.example.PixelMageEcomerceProject.entity.Card;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public interface CollectionService {

    // Collection CRUD (user-owned)
    CollectionResponse createCollection(Integer customerId, CollectionRequestDTO request);
    CollectionResponse updateCollection(Integer customerId, Integer collectionId, CollectionRequestDTO request);
    void deleteCollection(Integer customerId, Integer collectionId);
    Optional<CollectionResponse> getCollectionById(Integer collectionId);
    List<CollectionResponse> getCollectionsByCustomerId(Integer customerId);
    List<CollectionResponse> getPublicCollections();
    Page<CollectionResponse> getPublicCollections(Pageable pageable);

    // Admin-controlled collections
    CollectionResponse createAdminCollection(Integer adminId, AdminCollectionRequestDTO request);
    CollectionResponse updateCollectionVisibility(Integer collectionId, Boolean isVisible);

    // Collection Items
    CollectionItemResponse addCardToCollection(Integer customerId, CollectionItemRequestDTO request);
    void removeCardFromCollection(Integer customerId, Integer collectionId, Integer cardId);
    List<CollectionItemResponse> getCollectionItems(Integer collectionId);

    // Owned cards (purchased cards that can be added to collection)
    List<Card> getOwnedCards(Integer customerId);
    boolean isCardOwnedByCustomer(Integer customerId, Integer cardId);
}
