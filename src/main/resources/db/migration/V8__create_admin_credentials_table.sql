
CREATE TABLE admin_credentials
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    last_login_at TIMESTAMP    NULL,

    CONSTRAINT fk_admin_credentials_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);