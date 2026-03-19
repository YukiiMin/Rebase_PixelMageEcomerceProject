package com.example.PixelMageEcomerceProject.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PixelMageEcomerceProject.entity.PmPointWallet;

@Repository
public interface PmPointWalletRepository extends JpaRepository<PmPointWallet, Long> {
    Optional<PmPointWallet> findByUserId(Integer userId);
}
