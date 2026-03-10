package com.example.PixelMageEcomerceProject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.CollectionReward;

@Repository
public interface CollectionRewardRepository extends JpaRepository<CollectionReward, Integer> {

    boolean existsByUser_CustomerIdAndCollection_CollectionId(Integer userId, Integer collectionId);
}

