ALTER TABLE attendance_workers ADD COLUMN owner_email VARCHAR(254);
UPDATE attendance_workers SET owner_email='udaykiran@gmail.com';
ALTER TABLE attendance_workers ALTER COLUMN owner_email SET NOT NULL;
DROP INDEX IF EXISTS uq_attendance_workers_employee_code;
CREATE UNIQUE INDEX uq_attendance_workers_owner_employee_code
    ON attendance_workers(owner_email, upper(employee_code)) WHERE employee_code IS NOT NULL;
CREATE INDEX idx_attendance_workers_owner_name ON attendance_workers(owner_email, name);

ALTER TABLE properties ADD COLUMN owner_email VARCHAR(254);
UPDATE properties SET owner_email='udaykiran@gmail.com';
ALTER TABLE properties ALTER COLUMN owner_email SET NOT NULL;
CREATE UNIQUE INDEX uq_properties_owner ON properties(owner_email);

ALTER TABLE helpdesk_tickets ADD COLUMN owner_email VARCHAR(254);
UPDATE helpdesk_tickets SET owner_email='udaykiran@gmail.com';
ALTER TABLE helpdesk_tickets ALTER COLUMN owner_email SET NOT NULL;
CREATE INDEX idx_helpdesk_tickets_owner_created ON helpdesk_tickets(owner_email, created_at DESC);
