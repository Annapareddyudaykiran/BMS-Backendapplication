CREATE TABLE assets (id UUID PRIMARY KEY DEFAULT gen_random_uuid(), payload JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX idx_assets_created_at ON assets(created_at DESC);

