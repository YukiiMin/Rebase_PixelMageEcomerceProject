package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    Optional<Inventory> findByProductId(@Param("productId") Integer productId);
}

