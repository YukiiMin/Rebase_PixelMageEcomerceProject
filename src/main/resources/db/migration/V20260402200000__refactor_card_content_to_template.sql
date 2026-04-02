-- ============================================================
-- Migration: V20260402200000 — Refactor CARD_CONTENT
-- Purpose: Change FK from CARDS(card_id) → CARD_TEMPLATES(card_template_id)
--          Add: title, display_order, is_active columns
--          Drop: position (String, ambiguous), card_id (FK to physical card)
-- ============================================================

-- Step 1: Drop existing FK constraint and old columns
ALTER TABLE card_content
    DROP CONSTRAINT IF EXISTS fk_card_content_card,
    DROP COLUMN IF EXISTS card_id,
    DROP COLUMN IF EXISTS position;

-- Step 2: Add new columns
ALTER TABLE card_content
    ADD COLUMN IF NOT EXISTS card_template_id INTEGER        NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS title            VARCHAR(200),
    ADD COLUMN IF NOT EXISTS display_order    INTEGER        NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS is_active        BOOLEAN        NOT NULL DEFAULT TRUE;

-- Step 3: Drop the temporary default after adding column
ALTER TABLE card_content
    ALTER COLUMN card_template_id DROP DEFAULT;

-- Step 4: Add FK constraint to card_templates
ALTER TABLE card_content
    ADD CONSTRAINT fk_card_content_template
        FOREIGN KEY (card_template_id)
        REFERENCES card_templates(card_template_id)
        ON DELETE CASCADE;

-- Step 5: Update content_type column size and set NOT NULL
ALTER TABLE card_content
    ALTER COLUMN content_type TYPE VARCHAR(20),
    ALTER COLUMN content_type SET NOT NULL;

-- Step 6: Add index for performance when querying by template
CREATE INDEX IF NOT EXISTS idx_card_content_template_id
    ON card_content(card_template_id);

-- Note: If CARD_CONTENT had existing data pointing to old card_id FK,
-- those rows would have been dropped. Table was empty (no init data existed).
