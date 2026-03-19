package com.example.PixelMageEcomerceProject.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.ReadingSession;
import com.example.PixelMageEcomerceProject.enums.ReadingSessionStatus;

@Repository
public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Integer> {
    List<ReadingSession> findByAccount_CustomerId(Integer customerId);

    List<ReadingSession> findByAccount_CustomerIdAndMode(Integer customerId, com.example.PixelMageEcomerceProject.enums.ReadingSessionMode mode);

    Optional<ReadingSession> findFirstByAccount_CustomerIdAndStatusIn(
            Integer customerId, List<ReadingSessionStatus> statuses);
}

