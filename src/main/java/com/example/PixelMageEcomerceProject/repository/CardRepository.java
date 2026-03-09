package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
    Optional<Card> findByNfcUid(String nfcUid);

    List<Card> findByStatus(String status);
}
