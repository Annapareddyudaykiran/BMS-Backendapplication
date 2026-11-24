ALTER TABLE assets
  ADD COLUMN record_type VARCHAR(24) NOT NULL DEFAULT 'Asset',
  ADD COLUMN name VARCHAR(180),
  ADD COLUMN category VARCHAR(80),
  ADD COLUMN location VARCHAR(240),
  ADD COLUMN condition VARCHAR(40),
  ADD COLUMN maintenance_task TEXT,
  ADD COLUMN frequency VARCHAR(40),
  ADD COLUMN next_due DATE;
UPDATE assets SET
  record_type=COALESCE(payload->>'recordType','Asset'), name=payload->>'name',
  category=payload->>'category', location=payload->>'location', condition=payload->>'condition',
  maintenance_task=payload->>'maintenanceTask', frequency=payload->>'frequency',
  next_due=NULLIF(payload->>'nextDue','')::date;
ALTER TABLE assets DROP COLUMN payload;
