package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.UserInventory;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.repository.UserInventoryRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.SetStoryService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserCollectionProgressService;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserInventoryServiceImpl implements UserInventoryService {

    private final UserInventoryRepository userInventoryRepository;
    private final AccountRepository accountRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final UserCollectionProgressService userCollectionProgressService;
    private final SetStoryService setStoryService;

    @Override
    public UserInventory upsertInventory(Integer userId, Integer cardTemplateId, int quantityChange) {
        Optional<UserInventory> existing = userInventoryRepository.findByUser_CustomerIdAndCardTemplate_CardTemplateId(
                userId,
                cardTemplateId);

        UserInventory savedInventory;
        if (existing.isPresent()) {
            UserInventory inventory = existing.get();
            inventory.setQuantity(inventory.getQuantity() + quantityChange);
            savedInventory = userInventoryRepository.save(inventory);
        } else {
            Account account = accountRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Account not found for id: " + userId));

            CardTemplate template = cardTemplateRepository.findById(cardTemplateId)
                    .orElseThrow(() -> new RuntimeException("CardTemplate not found for id: " + cardTemplateId));

            UserInventory newInventory = new UserInventory();
            newInventory.setUser(account);
            newInventory.setCardTemplate(template);
            newInventory.setQuantity(Math.max(0, quantityChange));

            savedInventory = userInventoryRepository.save(newInventory);
        }

        userCollectionProgressService.recalculateProgressForTemplate(userId, cardTemplateId);
        setStoryService.checkAndUnlockStories(userId);

        return savedInventory;
    }

    @Override
    public List<UserInventory> getUserInventory(Integer userId) {
        return userInventoryRepository.findByUser_CustomerId(userId);
    }
}
