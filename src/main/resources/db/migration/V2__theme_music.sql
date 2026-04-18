-- V2: Theme Music table for admin-managed background music
CREATE TABLE IF NOT EXISTS theme_music (
    music_id        BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    artist          VARCHAR(200),
    cloudinary_public_id VARCHAR(500) NOT NULL,
    url             VARCHAR(1000) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Only one active track at a time: enforce with partial unique index
CREATE UNIQUE INDEX IF NOT EXISTS uniq_active_theme_music ON theme_music (is_active) WHERE is_active = TRUE;
