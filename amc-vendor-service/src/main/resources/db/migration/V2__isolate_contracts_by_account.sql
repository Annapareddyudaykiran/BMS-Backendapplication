ALTER TABLE amc_contracts ADD COLUMN owner_email VARCHAR(254);
UPDATE amc_contracts SET owner_email='udaykiran@gmail.com';
ALTER TABLE amc_contracts ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_amc_contracts_owner_expiry ON amc_contracts(owner_email, contract_expiry);
