-- V3__refactor_product_pack.sql
-- Create pack_categories and adjust products and packs tables

CREATE TABLE pack_categories (
    pack_category_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url TEXT,
    cards_per_pack INTEGER NOT NULL DEFAULT 5,
    rarity_rates TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE products ADD COLUMN product_type VARCHAR(20) NOT NULL DEFAULT 'GACHA_PACK';
ALTER TABLE products ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE products ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE products ADD COLUMN pack_category_id INTEGER REFERENCES pack_categories(pack_category_id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN card_template_id INTEGER REFERENCES card_templates(card_template_id) ON DELETE SET NULL;

CREATE TABLE pack_category_card_pools (
    pack_category_id INTEGER NOT NULL REFERENCES pack_categories(pack_category_id) ON DELETE CASCADE,
    card_template_id INTEGER NOT NULL REFERENCES card_templates(card_template_id) ON DELETE CASCADE,
    PRIMARY KEY (pack_category_id, card_template_id)
);

-- Copy existing structure from products to pack_categories
INSERT INTO pack_categories (pack_category_id, name, description, image_url, cards_per_pack, rarity_rates, is_active, created_at, updated_at)
SELECT product_id, name, description, image_url, 5, '{"COMMON":60,"RARE":30,"LEGENDARY":10}', TRUE, created_at, updated_at
FROM products;

-- Set sequence
SELECT setval('pack_categories_pack_category_id_seq', COALESCE((SELECT MAX(pack_category_id) FROM pack_categories), 1));

-- Point existing products to these new pack_categories
UPDATE products SET pack_category_id = product_id, product_type = 'GACHA_PACK';

-- Copy existing product card pools
INSERT INTO pack_category_card_pools (pack_category_id, card_template_id)
SELECT product_id, card_template_id FROM product_card_pools;

-- Drop old product_card_pools table
DROP TABLE product_card_pools;

-- Now update packs
ALTER TABLE packs ADD COLUMN pack_category_id INTEGER REFERENCES pack_categories(pack_category_id) ON DELETE SET NULL;

UPDATE packs SET pack_category_id = product_id;

ALTER TABLE packs DROP COLUMN product_id;

-- Update card_templates
ALTER TABLE card_templates ADD COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE;

-- Update pack_details
ALTER TABLE pack_details ALTER COLUMN card_id DROP NOT NULL;
ALTER TABLE pack_details ADD COLUMN card_template_id INTEGER REFERENCES card_templates(card_template_id) ON DELETE SET NULL;
