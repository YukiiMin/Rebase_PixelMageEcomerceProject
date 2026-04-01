-- TASK-05: UnlinkRequest Flow
-- Sprint 2.4, Phase 1
-- Created: 2026-03-20

CREATE TABLE unlink_requests (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     INT NOT NULL,
    nfc_uid         VARCHAR(100) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING_EMAIL',
    token           VARCHAR(36) NOT NULL,
    token_expiry    TIMESTAMP NOT NULL,
    staff_note      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at     TIMESTAMP,
    CONSTRAINT uq_unlink_request_token UNIQUE (token),
    CONSTRAINT fk_unlink_request_customer FOREIGN KEY (customer_id)
        REFERENCES Accounts(customer_id)
);

CREATE INDEX idx_unlink_requests_status ON unlink_requests(status);
CREATE INDEX idx_unlink_requests_token  ON unlink_requests(token);
