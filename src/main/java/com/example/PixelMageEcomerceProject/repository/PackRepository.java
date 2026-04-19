package com.example.PixelMageEcomerceProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Pack;
import com.example.PixelMageEcomerceProject.enums.PackStatus;

@Repository
public interface PackRepository extends JpaRepository<Pack, Integer> {

    @EntityGraph(value = "Pack.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    List<Pack> findAll();

    @EntityGraph(value = "Pack.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Page<Pack> findAll(Pageable pageable);

    @EntityGraph(value = "Pack.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    List<Pack> findByStatus(PackStatus status);

    @EntityGraph(value = "Pack.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    List<Pack> findByPackCategoryPackCategoryIdAndStatus(Integer packCategoryId, PackStatus status);

    long countByPackCategory_PackCategoryIdAndStatus(Integer packCategoryId, PackStatus status);
}
