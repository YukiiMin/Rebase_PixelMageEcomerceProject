package com.example.PixelMageEcomerceProject.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    List<Promotion> findByDiscountType(String discountType);

    List<Promotion> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
}
