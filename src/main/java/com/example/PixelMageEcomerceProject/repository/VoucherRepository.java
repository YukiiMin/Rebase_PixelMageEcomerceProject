package com.example.PixelMageEcomerceProject.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PixelMageEcomerceProject.entity.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByOwnerIdAndIsUsedFalseAndExpiresAtAfter(Integer ownerId, LocalDateTime date);
}
