package com.example.PixelMageEcomerceProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Pack;

@Repository
public interface PackRepository extends JpaRepository<Pack, Integer> {
    List<Pack> findByStatus(String status);

    List<Pack> findByProductProductIdAndStatus(Integer productId, String status);
}
