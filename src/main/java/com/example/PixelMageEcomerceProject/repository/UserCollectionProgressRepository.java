package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.UserCollectionProgress;

@Repository
public interface UserCollectionProgressRepository extends JpaRepository<UserCollectionProgress, Integer> {
    Optional<UserCollectionProgress> findByUser_CustomerIdAndCollection_CollectionId(Integer userId,
            Integer collectionId);

    List<UserCollectionProgress> findByUser_CustomerId(Integer userId);
}
