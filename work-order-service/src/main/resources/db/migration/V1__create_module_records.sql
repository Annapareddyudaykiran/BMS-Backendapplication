CREATE TABLE work_orders (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), payload JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX idx_work_orders_created_at ON work_orders(created_at DESC);

