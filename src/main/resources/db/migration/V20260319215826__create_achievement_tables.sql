-- TASK-03: Achievement System (Stub Architecture)
-- Sprint 2.4, Wave 3, Phase 1

CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    condition_type VARCHAR(50) NOT NULL,
    condition_value INT NOT NULL,
    pm_point_reward INT NOT NULL DEFAULT 0,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    granted_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id),
    CONSTRAINT fk_user_achievement_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(id)
);

-- Seed 3 achievements for testing (2 visible, 1 hidden)
-- Collector Novice: own 5 linked cards
-- Card Master: own 20 linked cards
-- Secret Archivist: complete 1 collection (HIDDEN until earned)
INSERT INTO achievements (name, description, condition_type, condition_value, pm_point_reward, is_hidden) VALUES
    ('Collector Novice', 'Sở hữu 5 thẻ bài', 'CARD_COUNT', 5, 50, false),
    ('Card Master', 'Sở hữu 20 thẻ bài', 'CARD_COUNT', 20, 200, false),
    ('Secret Archivist', '???', 'COLLECTION_COMPLETE', 1, 500, true);
