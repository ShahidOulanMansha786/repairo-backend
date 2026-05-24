ALTER TABLE repair_shops
    ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'INCOMPLETE',
    ADD COLUMN rejection_reason TEXT NULL,
    ADD COLUMN approved_at TIMESTAMP NULL,
    ADD COLUMN rejected_at TIMESTAMP NULL;
