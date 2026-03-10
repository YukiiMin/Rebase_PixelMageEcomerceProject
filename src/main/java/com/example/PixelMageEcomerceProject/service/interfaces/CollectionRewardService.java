package com.example.PixelMageEcomerceProject.service.interfaces;

import org.springframework.stereotype.Service;

@Service
public interface CollectionRewardService {

    void grantReward(Integer userId, Integer collectionId);
}

