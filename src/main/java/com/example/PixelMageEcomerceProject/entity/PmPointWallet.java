package com.example.PixelMageEcomerceProject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PmPointWallet — one wallet per user.
 * balance is always >= 0. Decrements happen atomically via redeemPoints().
 * Sprint 2.4: earn via Achievement grant only. Phase 2: other earn sources.
 */
@Entity
@Table(name = "pm_point_wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PmPointWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK to Accounts.customer_id. One-to-one by unique constraint on DB.
     * Not modelled as @OneToOne to keep Achievement system isolated from Account entity.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "balance", nullable = false)
    private Integer balance = 0;
}
