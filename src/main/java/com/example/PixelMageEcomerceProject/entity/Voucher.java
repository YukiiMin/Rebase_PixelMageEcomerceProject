package com.example.PixelMageEcomerceProject.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
 * Voucher — generated when user redeems 1000 PM_point.
 * Sprint 2.4 rates (confirmed by Mapper March 2026):
 * discountPct = 10 (10% off)
 * maxDiscountVnd = 20000 (cap 20,000 VND)
 */
@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID-based unique code used by customer at checkout. */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /** Discount percentage — always 10 in Sprint 2.4. */
    @Column(name = "discount_pct", nullable = false)
    private Integer discountPct = 10;

    /** Maximum discount in VND — always 20,000 in Sprint 2.4. */
    @Column(name = "max_discount_vnd", nullable = false)
    private Integer maxDiscountVnd = 20000;

    /** User who owns this voucher. FK → Accounts.customer_id. */
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
}
