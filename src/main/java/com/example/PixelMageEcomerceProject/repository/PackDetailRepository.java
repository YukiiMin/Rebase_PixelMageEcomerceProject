package com.example.PixelMageEcomerceProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.PackDetail;

@Repository
public interface PackDetailRepository extends JpaRepository<PackDetail, Integer> {
    List<PackDetail> findByPackPackId(Integer packId);
}
