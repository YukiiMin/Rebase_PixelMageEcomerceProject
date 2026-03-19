-- TASK-04: PM_point Wallet + Voucher System
-- Sprint 2.4, Phase 1

CREATE TABLE pm_point_wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    balance INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_pm_point_wallet_user UNIQUE (user_id)
);

CREATE TABLE vouchers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_pct INT NOT NULL DEFAULT 10,
    max_discount_vnd INT NOT NULL DEFAULT 20000,
    owner_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE
);
