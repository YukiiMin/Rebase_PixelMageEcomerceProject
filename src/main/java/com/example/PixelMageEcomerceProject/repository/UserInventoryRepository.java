package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.UserInventory;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, Integer> {
    Optional<UserInventory> findByUser_CustomerIdAndCardTemplate_CardTemplateId(Integer userId, Integer cardTemplateId);

    List<UserInventory> findByUser_CustomerId(Integer userId);
}
