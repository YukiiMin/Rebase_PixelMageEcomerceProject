package com.example.PixelMageEcomerceProject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.SetStory;
import com.example.PixelMageEcomerceProject.entity.UserInventory;
import com.example.PixelMageEcomerceProject.entity.UserStoryUnlock;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.SetStoryRepository;
import com.example.PixelMageEcomerceProject.repository.UserInventoryRepository;
import com.example.PixelMageEcomerceProject.repository.UserStoryUnlockRepository;
import com.example.PixelMageEcomerceProject.service.impl.SetStoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class SetStoryServiceImplTest {

    @Mock
    private SetStoryRepository setStoryRepository;
    @Mock
    private UserStoryUnlockRepository userStoryUnlockRepository;
    @Mock
    private UserInventoryRepository userInventoryRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private SetStoryServiceImpl setStoryService;

    @Test
    void revokeStoriesIfConditionNotMet_softRevokes_neverDeletes() {
        // Arrange
        Integer userId = 1;
        Account user = new Account();
        user.setCustomerId(userId);

        SetStory story = new SetStory();
        story.setStoryId(10);
        story.setRequiredTemplateIds("[1,2]");

        UserStoryUnlock unlock = new UserStoryUnlock();
        unlock.setUnlockId(100);
        unlock.setUser(user);
        unlock.setStory(story);
        unlock.setIsActive(true);

        // User currently only has template 1, meaning they lost template 2 (e.g. unlinked card)
        UserInventory inv = new UserInventory();
        CardTemplate ct = new CardTemplate();
        ct.setCardTemplateId(1);
        inv.setCardTemplate(ct);
        inv.setQuantity(1);

        when(userStoryUnlockRepository.findByUser_CustomerIdAndIsActiveTrue(userId))
                .thenReturn(List.of(unlock));
        when(userInventoryRepository.findByUser_CustomerId(userId))
                .thenReturn(List.of(inv));

        // Act
        setStoryService.revokeStoriesIfConditionNotMet(userId);

        // Assert that delete is NEVER called
        verify(userStoryUnlockRepository, never()).delete(any());
        verify(userStoryUnlockRepository, never()).deleteById(any());

        // Assert that save is called with isActive = false
        ArgumentCaptor<UserStoryUnlock> captor = ArgumentCaptor.forClass(UserStoryUnlock.class);
        verify(userStoryUnlockRepository).save(captor.capture());
        
        UserStoryUnlock savedUnlock = captor.getValue();
        assertThat(savedUnlock.getIsActive()).isFalse();
    }
}
