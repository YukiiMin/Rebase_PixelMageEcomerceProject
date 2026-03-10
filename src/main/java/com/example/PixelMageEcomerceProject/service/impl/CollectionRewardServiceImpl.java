package com.example.PixelMageEcomerceProject.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.CardCollection;
import com.example.PixelMageEcomerceProject.entity.CollectionReward;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardCollectionRepository;
import com.example.PixelMageEcomerceProject.repository.CollectionRewardRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.CollectionRewardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionRewardServiceImpl implements CollectionRewardService {

    private final CollectionRewardRepository rewardRepository;
    private final CardCollectionRepository collectionRepository;
    private final AccountRepository accountRepository;

    @Override
    public void grantReward(Integer userId, Integer collectionId) {
        if (rewardRepository.existsByUser_CustomerIdAndCollection_CollectionId(userId, collectionId)) {
            return;
        }

        CardCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found: " + collectionId));

        if (collection.getRewardType() == null) {
            return;
        }

        Account user = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + userId));

        CollectionReward reward = new CollectionReward();
        reward.setUser(user);
        reward.setCollection(collection);
        reward.setRewardType(collection.getRewardType());
        reward.setRewardData(collection.getRewardData());
        reward.setGrantedAt(LocalDateTime.now());

        rewardRepository.save(reward);
        // TODO: apply reward side-effects (points, voucher, etc.) based on rewardType/rewardData
    }
}

