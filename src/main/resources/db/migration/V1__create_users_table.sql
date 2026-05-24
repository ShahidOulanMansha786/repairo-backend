
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    phone       VARCHAR(20)   NULL,
    role        VARCHAR(20)   NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NULL
);