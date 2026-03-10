package com.example.PixelMageEcomerceProject.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.request.AdminCollectionItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.AdminCollectionRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.CollectionItemRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.CollectionRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.CardCollection;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.CollectionItem;
import com.example.PixelMageEcomerceProject.entity.Order;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardCollectionRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.repository.CollectionItemRepository;
import com.example.PixelMageEcomerceProject.repository.OrderRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CollectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionServiceImpl implements CollectionService {

    private final CardCollectionRepository cardCollectionRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final AccountRepository accountRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final OrderRepository orderRepository;

    // ==================== Collection CRUD ====================

    @Override
    public CardCollection createCollection(Integer customerId, CollectionRequestDTO request) {
        Account account = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));

        CardCollection collection = new CardCollection();
        collection.setAccount(account);
        collection.setCollectionName(request.getCollectionName());
        collection.setDescription(request.getDescription());
        Boolean isPublic = request.getIsPublic() != null ? request.getIsPublic() : false;
        collection.setIsPublic(isPublic);
        collection.setIsVisible(isPublic);
        collection.setSource("USER");

        return cardCollectionRepository.save(collection);
    }

    @Override
    public CardCollection updateCollection(Integer customerId, Integer collectionId, CollectionRequestDTO request) {
        CardCollection collection = cardCollectionRepository
                .findByCollectionIdAndAccountCustomerId(collectionId, customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found with id: " + collectionId + " for customer: " + customerId));

        if (request.getCollectionName() != null) {
            collection.setCollectionName(request.getCollectionName());
        }
        if (request.getDescription() != null) {
            collection.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            collection.setIsPublic(request.getIsPublic());
        }

        return cardCollectionRepository.save(collection);
    }

    @Override
    public void deleteCollection(Integer customerId, Integer collectionId) {
        CardCollection collection = cardCollectionRepository
                .findByCollectionIdAndAccountCustomerId(collectionId, customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found with id: " + collectionId + " for customer: " + customerId));

        collection.setIsActive(false);
        cardCollectionRepository.save(collection);
    }

    @Override
    public Optional<CardCollection> getCollectionById(Integer collectionId) {
        return cardCollectionRepository.findById(collectionId);
    }

    @Override
    public List<CardCollection> getCollectionsByCustomerId(Integer customerId) {
        return cardCollectionRepository.findByAccountCustomerId(customerId);
    }

    @Override
    public List<CardCollection> getPublicCollections() {
        return cardCollectionRepository.findAllVisibleCollections(LocalDateTime.now());
    }

    @Override
    public CardCollection createAdminCollection(Integer adminId, AdminCollectionRequestDTO request) {
        Account admin = accountRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin account not found with id: " + adminId));

        CardCollection collection = new CardCollection();
        collection.setCollectionName(request.getCollectionName());
        collection.setDescription(request.getDescription());
        collection.setCollectionType(
                request.getCollectionType() != null ? request.getCollectionType() : "STANDARD");
        collection.setStartTime(request.getStartTime());
        collection.setEndTime(request.getEndTime());
        collection.setRewardType(request.getRewardType());
        collection.setRewardData(request.getRewardData());
        collection.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : Boolean.FALSE);
        collection.setIsPublic(true);
        collection.setSource("SYSTEM");
        collection.setCreatedByAdmin(admin);
        collection.setAccount(admin); // Set owner for admin collection

        CardCollection savedCollection = cardCollectionRepository.save(collection);

        if (request.getItems() != null) {
            for (AdminCollectionItemRequestDTO itemDto : request.getItems()) {
                CardTemplate cardTemplate = cardTemplateRepository.findById(itemDto.getCardTemplateId())
                        .orElseThrow(() -> new RuntimeException(
                                "CardTemplate not found with id: " + itemDto.getCardTemplateId()));

                CollectionItem item = new CollectionItem();
                item.setCardCollection(savedCollection);
                item.setCardTemplate(cardTemplate);
                item.setRequiredQuantity(
                        itemDto.getRequiredQuantity() != null ? itemDto.getRequiredQuantity() : 1);

                collectionItemRepository.save(item);
            }
        }

        return savedCollection;
    }

    @Override
    public CardCollection updateCollectionVisibility(Integer collectionId, Boolean isVisible) {
        CardCollection collection = cardCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));
        collection.setIsVisible(isVisible != null ? isVisible : collection.getIsVisible());
        return cardCollectionRepository.save(collection);
    }

    // ==================== Collection Items ====================

    @Override
    public CollectionItem addCardToCollection(Integer customerId, CollectionItemRequestDTO request) {
        // 1. Verify collection belongs to customer (Admin)
        CardCollection collection = cardCollectionRepository
                .findByCollectionIdAndAccountCustomerId(request.getCollectionId(), customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found with id: " + request.getCollectionId() + " for customer: " + customerId));

        // 2. Verify cardTemplate exists
        CardTemplate cardTemplate = cardTemplateRepository.findById(request.getCardTemplateId())
                .orElseThrow(
                        () -> new RuntimeException("CardTemplate not found with id: " + request.getCardTemplateId()));

        // 3. Check if cardTemplate is already in collection
        if (collectionItemRepository.existsByCardCollectionCollectionIdAndCardTemplateCardTemplateId(
                request.getCollectionId(), request.getCardTemplateId())) {
            throw new RuntimeException("Card template is already in this collection.");
        }

        // 4. Add template to collection
        CollectionItem item = new CollectionItem();
        item.setCardCollection(collection);
        item.setCardTemplate(cardTemplate);
        item.setRequiredQuantity(request.getRequiredQuantity() != null ? request.getRequiredQuantity() : 1);

        return collectionItemRepository.save(item);
    }

    @Override
    public void removeCardFromCollection(Integer customerId, Integer collectionId, Integer cardTemplateId) {
        // Verify collection belongs to customer
        cardCollectionRepository.findByCollectionIdAndAccountCustomerId(collectionId, customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Collection not found with id: " + collectionId + " for customer: " + customerId));

        // Verify item exists
        CollectionItem item = collectionItemRepository
                .findByCardCollectionCollectionIdAndCardTemplateCardTemplateId(collectionId, cardTemplateId)
                .orElseThrow(() -> new RuntimeException(
                        "CardTemplate with id: " + cardTemplateId + " not found in collection: " + collectionId));

        collectionItemRepository.delete(item);
    }

    @Override
    public List<CollectionItem> getCollectionItems(Integer collectionId) {
        return collectionItemRepository.findByCardCollectionCollectionId(collectionId);
    }

    // ==================== Owned Cards ====================

    /**
     * Get all cards owned by a customer.
     * A card is "owned" when it was purchased through a COMPLETED order with PAID
     * payment status.
     *
     * Chain: Account -> Order (COMPLETED + PAID) -> OrderItem -> Card
     */
    @Override
    public List<Card> getOwnedCards(Integer customerId) {
        List<Order> completedOrders = orderRepository.findByAccountCustomerId(customerId)
                .stream()
                .filter(order -> "COMPLETED".equals(order.getStatus()) && "PAID".equals(order.getPaymentStatus()))
                .collect(Collectors.toList());

        return completedOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .flatMap(orderItem -> orderItem.getPack().getPackDetails().stream())
                .map(packDetail -> packDetail.getCard())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific card is owned by a customer.
     */
    @Override
    public boolean isCardOwnedByCustomer(Integer customerId, Integer cardId) {
        return orderRepository.findByAccountCustomerId(customerId)
                .stream()
                .filter(order -> "COMPLETED".equals(order.getStatus()) && "PAID".equals(order.getPaymentStatus()))
                .flatMap(order -> order.getOrderItems().stream())
                .flatMap(orderItem -> orderItem.getPack().getPackDetails().stream())
                .anyMatch(packDetail -> packDetail.getCard().getCardId().equals(cardId));
    }
}
