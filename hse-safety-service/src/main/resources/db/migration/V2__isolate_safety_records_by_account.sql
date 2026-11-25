ALTER TABLE safety_reports ADD COLUMN owner_email VARCHAR(254);
UPDATE safety_reports SET owner_email='udaykiran@gmail.com';
ALTER TABLE safety_reports ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_safety_reports_owner_created ON safety_reports(owner_email, created_at DESC);

ALTER TABLE safety_inspections ADD COLUMN owner_email VARCHAR(254);
UPDATE safety_inspections SET owner_email='udaykiran@gmail.com';
ALTER TABLE safety_inspections ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_safety_inspections_owner_due ON safety_inspections(owner_email, due_date);
