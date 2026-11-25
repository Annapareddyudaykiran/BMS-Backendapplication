ALTER TABLE work_orders ADD COLUMN owner_email VARCHAR(254);
UPDATE work_orders SET owner_email='udaykiran@gmail.com';
ALTER TABLE work_orders ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_work_orders_owner_created ON work_orders(owner_email, created_at DESC);
