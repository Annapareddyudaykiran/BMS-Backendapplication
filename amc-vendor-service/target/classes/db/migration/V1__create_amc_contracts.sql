CREATE TABLE amc_contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_no BIGSERIAL UNIQUE NOT NULL,
    vendor_name VARCHAR(180) NOT NULL,
    covered_asset VARCHAR(180) NOT NULL,
    contract_start DATE NOT NULL,
    contract_expiry DATE NOT NULL,
    annual_value NUMERIC(14,2) NOT NULL DEFAULT 0,
    service_frequency VARCHAR(40) NOT NULL,
    vendor_contact VARCHAR(180),
    scope TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'Active',
    work_order_reference VARCHAR(80),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_amc_dates CHECK (contract_expiry >= contract_start)
);
CREATE INDEX idx_amc_contract_expiry ON amc_contracts(contract_expiry);
CREATE INDEX idx_amc_vendor_name ON amc_contracts(vendor_name);
