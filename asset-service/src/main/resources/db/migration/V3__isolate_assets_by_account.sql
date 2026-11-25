ALTER TABLE assets ADD COLUMN owner_email VARCHAR(254);
UPDATE assets SET owner_email='udaykiran@gmail.com';
ALTER TABLE assets ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_assets_owner_created ON assets(owner_email, created_at DESC);
