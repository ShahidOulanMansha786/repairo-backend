CREATE TABLE leads (
   id           BIGSERIAL     PRIMARY KEY,
   car_owner_id BIGINT        NOT NULL,
   title        VARCHAR(255)  NOT NULL,
   description  TEXT          NOT NULL,
   car_make     VARCHAR(100)  NOT NULL,
   car_model    VARCHAR(100)  NOT NULL,
   car_year     INTEGER       NOT NULL,
   address      TEXT          NOT NULL,
   location     geometry(Point, 4326) NOT NULL,
   status       VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
   created_at   TIMESTAMP     NOT NULL,
   expires_at   TIMESTAMP     NULL,

   CONSTRAINT fk_leads_car_owner FOREIGN KEY (car_owner_id) REFERENCES users(id)
);