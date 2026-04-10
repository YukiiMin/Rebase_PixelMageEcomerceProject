-- ============================================================
-- V1__baseline_schema.sql
-- Baseline schema for PixelMage — generated from JPA entities.
-- Run once on a clean DB. All subsequent changes via new Vx__ scripts.
-- ============================================================

-- ----------------------------------------------------------------
-- 1. Independent tables (no FK dependencies)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    role_id   SERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS card_frameworks (
    framework_id SERIAL PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL,
    description  VARCHAR(500),
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS spreads (
    spread_id          SERIAL PRIMARY KEY,
    name               VARCHAR(100) NOT NULL UNIQUE,
    description        VARCHAR(500),
    position_count     INTEGER NOT NULL,
    min_cards_required INTEGER
);

CREATE TABLE IF NOT EXISTS achievements (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    condition_type   VARCHAR(50)  NOT NULL,
    condition_value  INTEGER      NOT NULL,
    pm_point_reward  INTEGER      NOT NULL DEFAULT 0,
    is_hidden        BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS promotions (
    promotion_id   SERIAL PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    discount_type  VARCHAR(20)    NOT NULL,
    discount_value NUMERIC(10, 2) NOT NULL,
    start_date     TIMESTAMP      NOT NULL,
    end_date       TIMESTAMP      NOT NULL,
    order_id       INTEGER,
    created_at     TIMESTAMP      NOT NULL,
    updated_at     TIMESTAMP      NOT NULL
);

CREATE TABLE IF NOT EXISTS set_stories (
    story_id            SERIAL PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    content             TEXT,
    required_template_ids TEXT,
    cover_image_path    VARCHAR(500),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    product_id  SERIAL PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    image_url   VARCHAR(255),
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL
);

-- ----------------------------------------------------------------
-- 2. card_templates (depends on card_frameworks)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS card_templates (
    card_template_id SERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    description      TEXT,
    design_path      VARCHAR(255),
    arcana_type      VARCHAR(50),
    suit             VARCHAR(50),
    card_number      INTEGER,
    rarity           VARCHAR(50),
    framework_id     INTEGER REFERENCES card_frameworks(framework_id),
    is_active        BOOLEAN   NOT NULL DEFAULT TRUE,
    image_path       VARCHAR(500),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

-- ----------------------------------------------------------------
-- 3. accounts (depends on roles)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    customer_id              SERIAL PRIMARY KEY,
    email                    VARCHAR(100) NOT NULL UNIQUE,
    password                 VARCHAR(100),
    name                     VARCHAR(100) NOT NULL,
    phone_number             VARCHAR(20),
    avatar_url               VARCHAR(500),
    gender                   VARCHAR(10),
    date_of_birth            DATE,
    address                  VARCHAR(255),
    email_verified           BOOLEAN   NOT NULL DEFAULT FALSE,
    verification_token       VARCHAR(200),
    verification_token_expiry TIMESTAMP,
    auth_provider            VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id              VARCHAR(100),
    created_at               TIMESTAMP NOT NULL,
    updated_at               TIMESTAMP NOT NULL,
    is_active                BOOLEAN   NOT NULL DEFAULT TRUE,
    guest_reading_used_at    TIMESTAMP,
    role_id                  INTEGER   NOT NULL REFERENCES roles(role_id)
);

-- ----------------------------------------------------------------
-- 4. Content & helpers (depends on card_templates)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS divine_helpers (
    divine_helper_id SERIAL PRIMARY KEY,
    card_template_id INTEGER NOT NULL REFERENCES card_templates(card_template_id),
    upright_meaning  TEXT,
    reversed_meaning TEXT,
    zodiac_sign      VARCHAR(50),
    element          VARCHAR(50),
    keywords         VARCHAR(500),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS card_content (
    content_id       SERIAL PRIMARY KEY,
    card_template_id INTEGER     NOT NULL REFERENCES card_templates(card_template_id),
    title            VARCHAR(200),
    content_type     VARCHAR(20) NOT NULL,
    content_data     TEXT        NOT NULL,
    display_order    INTEGER     NOT NULL DEFAULT 1,
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP   NOT NULL
);

-- ----------------------------------------------------------------
-- 5. Physical inventory tables (depends on products)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id   INTEGER   NOT NULL REFERENCES products(product_id),
    quantity     INTEGER   NOT NULL,
    last_checked TIMESTAMP,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cards (
    card_id          SERIAL PRIMARY KEY,
    nfc_uid          VARCHAR(255) UNIQUE,
    software_uuid    VARCHAR(255) UNIQUE,
    card_template_id INTEGER     NOT NULL REFERENCES card_templates(card_template_id),
    product_id       INTEGER     NOT NULL REFERENCES products(product_id),
    custom_text      TEXT,
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP   NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING_BIND',
    serial_number    VARCHAR(100),
    production_batch VARCHAR(100),
    card_condition   VARCHAR(20) DEFAULT 'NEW',
    owner_account_id INTEGER REFERENCES accounts(customer_id),
    linked_at        TIMESTAMP,
    sold_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS packs (
    pack_id              SERIAL PRIMARY KEY,
    version              BIGINT      NOT NULL DEFAULT 0,
    product_id           INTEGER     NOT NULL REFERENCES products(product_id),
    status               VARCHAR(20) NOT NULL DEFAULT 'STOCKED',
    created_by_account_id INTEGER REFERENCES accounts(customer_id),
    created_at           TIMESTAMP   NOT NULL
);

CREATE TABLE IF NOT EXISTS pack_details (
    pack_detail_id SERIAL PRIMARY KEY,
    pack_id        INTEGER NOT NULL REFERENCES packs(pack_id),
    card_id        INTEGER NOT NULL REFERENCES cards(card_id),
    position_index INTEGER
);

-- Many-to-many: Product ↔ CardTemplate (gacha pool)
CREATE TABLE IF NOT EXISTS product_card_pools (
    product_id       INTEGER NOT NULL REFERENCES products(product_id),
    card_template_id INTEGER NOT NULL REFERENCES card_templates(card_template_id),
    PRIMARY KEY (product_id, card_template_id)
);

-- ----------------------------------------------------------------
-- 6. Orders & Payments (depends on accounts, products, packs)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id         SERIAL PRIMARY KEY,
    customer_id      INTEGER        NOT NULL REFERENCES accounts(customer_id),
    order_date       TIMESTAMP      NOT NULL,
    status           VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    total_amount     NUMERIC(10, 2) NOT NULL,
    shipping_address VARCHAR(500),
    payment_method   VARCHAR(50),
    payment_status   VARCHAR(50),
    notes            TEXT,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id      INTEGER        NOT NULL REFERENCES orders(order_id),
    product_id    INTEGER        NOT NULL REFERENCES products(product_id),
    pack_id       INTEGER        REFERENCES packs(pack_id),  -- NULL until payment confirmed
    quantity      INTEGER        NOT NULL,
    unit_price    NUMERIC(10, 2) NOT NULL,
    subtotal      NUMERIC(10, 2) NOT NULL,
    custom_text   TEXT,
    created_at    TIMESTAMP      NOT NULL,
    updated_at    TIMESTAMP      NOT NULL
);

-- Add FK from promotions to orders (cycle-safe, added after orders exists)
ALTER TABLE promotions
    ADD COLUMN IF NOT EXISTS order_id_fk INTEGER REFERENCES orders(order_id);

CREATE TABLE IF NOT EXISTS payments (
    payment_id                SERIAL PRIMARY KEY,
    order_id                  INTEGER        NOT NULL REFERENCES orders(order_id),
    gateway_transaction_id    VARCHAR(100),
    payment_gateway           VARCHAR(50),
    stripe_customer_id        VARCHAR(100),
    stripe_payment_method_id  VARCHAR(100),
    amount                    NUMERIC(10, 2) NOT NULL,
    currency                  VARCHAR(3)     NOT NULL,
    payment_status            VARCHAR(50)    NOT NULL,
    payment_method            VARCHAR(50),
    client_secret             VARCHAR(200),
    failure_reason            VARCHAR(500),
    processing_fee            NUMERIC(10, 2),
    net_amount                NUMERIC(10, 2),
    is_saved_payment_method   BOOLEAN DEFAULT FALSE,
    created_at                TIMESTAMP NOT NULL,
    updated_at                TIMESTAMP NOT NULL,
    processed_at              TIMESTAMP
);

-- ----------------------------------------------------------------
-- 7. Collections (depends on accounts, card_templates)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS collections (
    collection_id      SERIAL PRIMARY KEY,
    collection_name    VARCHAR(255) NOT NULL,
    description        TEXT,
    customer_id        INTEGER      NOT NULL REFERENCES accounts(customer_id),
    is_public          BOOLEAN      DEFAULT FALSE,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    collection_type    VARCHAR(20)  DEFAULT 'STANDARD',
    start_time         TIMESTAMP,
    end_time           TIMESTAMP,
    is_visible         BOOLEAN      NOT NULL DEFAULT TRUE,
    reward_type        VARCHAR(50),
    reward_data        TEXT,
    created_by_admin_id INTEGER REFERENCES accounts(customer_id),
    source             VARCHAR(10)  NOT NULL DEFAULT 'SYSTEM'
);

CREATE TABLE IF NOT EXISTS collection_items (
    collection_item_id SERIAL PRIMARY KEY,
    collection_id      INTEGER NOT NULL REFERENCES collections(collection_id),
    card_template_id   INTEGER NOT NULL REFERENCES card_templates(card_template_id),
    required_quantity  INTEGER DEFAULT 1,
    added_at           TIMESTAMP NOT NULL,
    UNIQUE (collection_id, card_template_id)
);

CREATE TABLE IF NOT EXISTS collection_rewards (
    reward_id     SERIAL PRIMARY KEY,
    user_id       INTEGER   NOT NULL REFERENCES accounts(customer_id),
    collection_id INTEGER   NOT NULL REFERENCES collections(collection_id),
    reward_type   VARCHAR(50),
    reward_data   TEXT,
    granted_at    TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_collection_progress (
    id                 SERIAL PRIMARY KEY,
    user_id            INTEGER   NOT NULL REFERENCES accounts(customer_id),
    collection_id      INTEGER   NOT NULL REFERENCES collections(collection_id),
    owned_count        INTEGER   DEFAULT 0,
    required_count     INTEGER,
    completion_percent FLOAT     DEFAULT 0.0,
    is_completed       BOOLEAN   DEFAULT FALSE,
    completed_at       TIMESTAMP,
    last_updated_at    TIMESTAMP,
    UNIQUE (user_id, collection_id)
);

-- ----------------------------------------------------------------
-- 8. Reading sessions (depends on accounts, spreads, card_templates)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reading_sessions (
    session_id         SERIAL PRIMARY KEY,
    customer_id        INTEGER      NOT NULL REFERENCES accounts(customer_id),
    spread_id          INTEGER      NOT NULL REFERENCES spreads(spread_id),
    main_question      VARCHAR(500),
    mode               VARCHAR(50),
    status             VARCHAR(50),
    ai_interpretation  TEXT,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS reading_cards (
    reading_card_id  SERIAL PRIMARY KEY,
    session_id       INTEGER     NOT NULL REFERENCES reading_sessions(session_id),
    card_template_id INTEGER     NOT NULL REFERENCES card_templates(card_template_id),
    position_index   INTEGER     NOT NULL,
    position_name    VARCHAR(100),
    is_reversed      BOOLEAN     NOT NULL DEFAULT FALSE
);

-- ----------------------------------------------------------------
-- 9. User economy (depends on accounts, card_templates)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pm_point_wallets (
    id      BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE REFERENCES accounts(customer_id),
    balance INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS vouchers (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(50)  NOT NULL UNIQUE,
    discount_pct    INTEGER      NOT NULL DEFAULT 10,
    max_discount_vnd INTEGER     NOT NULL DEFAULT 20000,
    owner_id        INTEGER      NOT NULL REFERENCES accounts(customer_id),
    created_at      TIMESTAMP    NOT NULL,
    expires_at      TIMESTAMP    NOT NULL,
    is_used         BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id             BIGSERIAL PRIMARY KEY,
    user_id        INTEGER   NOT NULL,
    achievement_id BIGINT    NOT NULL REFERENCES achievements(id),
    granted_at     TIMESTAMP NOT NULL,
    is_active      BOOLEAN   NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, achievement_id)
);

CREATE TABLE IF NOT EXISTS user_inventory (
    id               SERIAL PRIMARY KEY,
    user_id          INTEGER   NOT NULL REFERENCES accounts(customer_id),
    card_template_id INTEGER   NOT NULL REFERENCES card_templates(card_template_id),
    quantity         INTEGER   NOT NULL DEFAULT 0,
    version          INTEGER,
    updated_at       TIMESTAMP,
    UNIQUE (user_id, card_template_id)
);

-- ----------------------------------------------------------------
-- 10. Story unlocks (depends on accounts, set_stories)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_story_unlocks (
    unlock_id   SERIAL PRIMARY KEY,
    user_id     INTEGER   NOT NULL REFERENCES accounts(customer_id),
    story_id    INTEGER   NOT NULL REFERENCES set_stories(story_id),
    unlocked_at TIMESTAMP NOT NULL,
    is_active   BOOLEAN   NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, story_id)
);

-- ----------------------------------------------------------------
-- 11. Unlink requests (depends on accounts)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS unlink_requests (
    id          BIGSERIAL PRIMARY KEY,
    customer_id INTEGER      NOT NULL REFERENCES accounts(customer_id),
    nfc_uid     VARCHAR(100) NOT NULL,
    status      VARCHAR(30)  NOT NULL,
    token       VARCHAR(36)  NOT NULL UNIQUE,
    token_expiry TIMESTAMP   NOT NULL,
    staff_note  VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL,
    resolved_at TIMESTAMP
);
