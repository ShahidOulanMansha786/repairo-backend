CREATE TABLE quotes (
    id             BIGSERIAL      PRIMARY KEY,
    lead_id        BIGINT         NOT NULL,
    repair_shop_id BIGINT         NOT NULL,
    price          NUMERIC(10,2)  NOT NULL,
    message        TEXT           NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP      NOT NULL,

    CONSTRAINT fk_quotes_lead       FOREIGN KEY (lead_id)        REFERENCES leads(id),
    CONSTRAINT fk_quotes_shop       FOREIGN KEY (repair_shop_id) REFERENCES repair_shops(id),
    CONSTRAINT uq_quotes_lead_shop  UNIQUE (lead_id, repair_shop_id)
);