ALTER TABLE repair_shops
    ALTER COLUMN logo_url DROP NOT NULL,
    ALTER COLUMN cnic_url DROP NOT NULL,
    ALTER COLUMN business_doc_url DROP NOT NULL;