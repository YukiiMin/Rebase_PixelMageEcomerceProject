ALTER TABLE packs
ADD COLUMN version BIGINT DEFAULT 0;

UPDATE packs SET version = 0 WHERE version IS NULL;
