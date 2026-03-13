 package com.example.PixelMageEcomerceProject.repository;

import com.example.PixelMageEcomerceProject.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    List<Promotion> findByDiscountType(String discountType);

    List<Promotion> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);
}

