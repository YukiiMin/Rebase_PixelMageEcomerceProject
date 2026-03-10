package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.CollectionItem;

@Repository
public interface CollectionItemRepository extends JpaRepository<CollectionItem, Integer> {

    List<CollectionItem> findByCardCollectionCollectionId(Integer collectionId);

    List<CollectionItem> findByCardTemplateCardTemplateId(Integer cardTemplateId);

    Optional<CollectionItem> findByCardCollectionCollectionIdAndCardTemplateCardTemplateId(Integer collectionId,
            Integer cardTemplateId);

    boolean existsByCardCollectionCollectionIdAndCardTemplateCardTemplateId(Integer collectionId,
            Integer cardTemplateId);

    void deleteByCardCollectionCollectionIdAndCardTemplateCardTemplateId(Integer collectionId, Integer cardTemplateId);

    @Query("SELECT COUNT(ci) FROM CollectionItem ci WHERE ci.cardCollection.collectionId = :collectionId")
    Long countByCollectionId(@Param("collectionId") Integer collectionId);
}
