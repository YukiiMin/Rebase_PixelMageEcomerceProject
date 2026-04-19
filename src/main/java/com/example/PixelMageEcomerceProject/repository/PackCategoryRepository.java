package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.PackCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackCategoryRepository extends JpaRepository<PackCategory, Integer> {
    
}
