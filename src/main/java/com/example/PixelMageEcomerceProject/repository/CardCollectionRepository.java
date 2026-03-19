package com.example.PixelMageEcomerceProject.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.CardCollection;

@Repository
public interface CardCollectionRepository extends JpaRepository<CardCollection, Integer> {

    List<CardCollection> findByAccountCustomerId(Integer customerId);

    List<CardCollection> findByAccountCustomerIdAndIsVisibleTrue(Integer customerId);

    Optional<CardCollection> findByCollectionIdAndAccountCustomerId(Integer collectionId, Integer customerId);

    @Query("SELECT c FROM CardCollection c WHERE c.isPublic = true")
    List<CardCollection> findAllPublicCollections();

    @Query("SELECT c FROM CardCollection c WHERE c.isVisible = true " +
            "AND (c.startTime IS NULL OR c.startTime <= :now) " +
            "AND (c.endTime IS NULL OR c.endTime >= :now)")
    List<CardCollection> findAllVisibleCollections(@Param("now") LocalDateTime now);
}
