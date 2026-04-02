package com.example.PixelMageEcomerceProject.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.response.SetStoryResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.SetStory;
import com.example.PixelMageEcomerceProject.entity.UserInventory;
import com.example.PixelMageEcomerceProject.entity.UserStoryUnlock;
import com.example.PixelMageEcomerceProject.exceptions.StoryNotUnlockedException;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.SetStoryRepository;
import com.example.PixelMageEcomerceProject.repository.UserInventoryRepository;
import com.example.PixelMageEcomerceProject.repository.UserStoryUnlockRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.SetStoryService;

import com.example.PixelMageEcomerceProject.mapper.SetStoryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SetStoryServiceImpl implements SetStoryService {

    private final SetStoryRepository setStoryRepository;
    private final UserStoryUnlockRepository userStoryUnlockRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final AccountRepository accountRepository;
    private final SetStoryMapper setStoryMapper;

    @Override
    public void checkAndUnlockStories(Integer userId) {
        Account user = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + userId));

        List<SetStory> activeStories = setStoryRepository.findByIsActiveTrue();
        if (activeStories.isEmpty()) {
            return;
        }

        List<UserInventory> inventoryList = userInventoryRepository.findByUser_CustomerId(userId);
        Set<Integer> ownedTemplateIds = new HashSet<>();
        for (UserInventory inv : inventoryList) {
            if (inv.getQuantity() != null && inv.getQuantity() > 0 && inv.getCardTemplate() != null) {
                ownedTemplateIds.add(inv.getCardTemplate().getCardTemplateId());
            }
        }

        for (SetStory story : activeStories) {
            List<Integer> requiredTemplateIds = parseTemplateIds(story.getRequiredTemplateIds());
            if (requiredTemplateIds.isEmpty()) {
                continue;
            }

            java.util.Optional<UserStoryUnlock> existingUnlock = userStoryUnlockRepository
                    .findByUser_CustomerIdAndStory_StoryId(userId, story.getStoryId());
            if (existingUnlock.isPresent()) {
                if (!existingUnlock.get().getIsActive()) {
                    existingUnlock.get().setIsActive(true);
                    userStoryUnlockRepository.save(existingUnlock.get());
                }
                continue;
            }

            boolean hasAllRequired = ownedTemplateIds.containsAll(requiredTemplateIds);
            if (!hasAllRequired) {
                continue;
            }

            UserStoryUnlock unlock = new UserStoryUnlock();
            unlock.setUser(user);
            unlock.setStory(story);
            unlock.setIsActive(true);
            userStoryUnlockRepository.save(unlock);
        }
    }

    @Override
    public void revokeStoriesIfConditionNotMet(Integer userId) {
        List<UserStoryUnlock> activeUnlocks = userStoryUnlockRepository.findByUser_CustomerIdAndIsActiveTrue(userId);
        if (activeUnlocks.isEmpty()) {
            return;
        }

        List<UserInventory> inventoryList = userInventoryRepository.findByUser_CustomerId(userId);
        Set<Integer> ownedTemplateIds = new HashSet<>();
        for (UserInventory inv : inventoryList) {
            if (inv.getQuantity() != null && inv.getQuantity() > 0 && inv.getCardTemplate() != null) {
                ownedTemplateIds.add(inv.getCardTemplate().getCardTemplateId());
            }
        }

        for (UserStoryUnlock unlock : activeUnlocks) {
            SetStory story = unlock.getStory();
            List<Integer> requiredTemplateIds = parseTemplateIds(story.getRequiredTemplateIds());
            
            boolean hasAllRequired = ownedTemplateIds.containsAll(requiredTemplateIds);
            if (!hasAllRequired) {
                unlock.setIsActive(false);
                userStoryUnlockRepository.save(unlock);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SetStory> getAllStories() {
        return setStoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SetStory> getUnlockedStories(Integer userId) {
        List<UserStoryUnlock> unlocks = userStoryUnlockRepository.findByUser_CustomerIdAndIsActiveTrue(userId);
        List<SetStory> stories = new ArrayList<>();
        for (UserStoryUnlock unlock : unlocks) {
            stories.add(unlock.getStory());
        }
        return stories;
    }

    @Override
    public SetStory createStory(SetStory story) {
        return setStoryRepository.save(story);
    }

    @Override
    @Transactional(readOnly = true)
    public SetStory getStoryById(Integer id) {
        return setStoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SetStoryResponse getStoryById(Integer storyId, Integer userId) {
        SetStory story = setStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));

        boolean isUnlocked = userStoryUnlockRepository
                .existsByUser_CustomerIdAndStory_StoryIdAndIsActiveTrue(userId, storyId);

        if (!isUnlocked) {
            throw new StoryNotUnlockedException(
                    "Bạn chưa mở khóa story này. Hoàn thành bộ thẻ để truy cập.");
        }

        return setStoryMapper.toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public SetStoryResponse getStoryByIdNoGate(Integer storyId) {
        SetStory story = setStoryRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));
        return setStoryMapper.toResponse(story);
    }


    @Override
    public SetStory updateStory(SetStory story) {
        return setStoryRepository.save(story);
    }

    @Override
    public void deleteStory(Integer id) {
        if (!setStoryRepository.existsById(id)) {
            throw new RuntimeException("Story not found with id: " + id);
        }
        setStoryRepository.deleteById(id);
    }

    private List<Integer> parseTemplateIds(String raw) {
        List<Integer> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }

        String cleaned = raw.trim();
        if (cleaned.startsWith("[")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("]")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        String[] parts = cleaned.split(",");
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }
            try {
                result.add(Integer.parseInt(token));
            } catch (NumberFormatException ignored) {
            }
        }

        return result;
    }
}

