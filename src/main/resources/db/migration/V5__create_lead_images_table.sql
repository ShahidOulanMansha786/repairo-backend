CREATE TABLE lead_images (
     id          BIGSERIAL PRIMARY KEY,
     lead_id     BIGINT    NOT NULL,
     image_url   TEXT      NOT NULL,
     uploaded_at TIMESTAMP NOT NULL,

     CONSTRAINT fk_lead_images_lead FOREIGN KEY (lead_id) REFERENCES leads(id)
);