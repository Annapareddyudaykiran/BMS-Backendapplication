ALTER TABLE inventory_items ADD COLUMN owner_email VARCHAR(254);
UPDATE inventory_items SET owner_email='udaykiran@gmail.com';
ALTER TABLE inventory_items ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_inventory_owner_name ON inventory_items(owner_email, name);

ALTER TABLE purchase_requests ADD COLUMN owner_email VARCHAR(254);
UPDATE purchase_requests SET owner_email='udaykiran@gmail.com';
ALTER TABLE purchase_requests ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_procurement_owner_created ON purchase_requests(owner_email, created_at DESC);
