CREATE TABLE disputes
(
    id          BIGSERIAL PRIMARY KEY,
    lead_id     BIGINT      NOT NULL UNIQUE REFERENCES leads (id),
    raised_by   BIGINT      NOT NULL REFERENCES users (id),
    reason      TEXT        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    admin_note  TEXT        NULL,
    created_at  TIMESTAMP   NOT NULL,
    resolved_at TIMESTAMP   NULL
);