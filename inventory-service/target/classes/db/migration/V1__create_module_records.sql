CREATE TABLE inventory_items (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), payload JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX idx_inventory_items_created_at ON inventory_items(created_at DESC);
CREATE TABLE purchase_requests (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), payload JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX idx_purchase_requests_created_at ON purchase_requests(created_at DESC);

