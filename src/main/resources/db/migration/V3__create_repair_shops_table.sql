
CREATE TABLE repair_shops (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT        NOT NULL UNIQUE,
    shop_name        VARCHAR(255)  NOT NULL,
    description      TEXT          NULL,
    phone            VARCHAR(20)   NOT NULL,
    address          TEXT          NOT NULL,
    location         geometry(Point, 4326) NOT NULL,
    logo_url         TEXT          NULL,
    cnic_url         TEXT          NOT NULL,
    business_doc_url TEXT          NOT NULL,
    is_verified      BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL,

    CONSTRAINT fk_repair_shops_user FOREIGN KEY (user_id) REFERENCES users(id)
);