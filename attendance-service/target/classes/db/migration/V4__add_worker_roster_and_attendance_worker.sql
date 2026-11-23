CREATE TABLE attendance_workers (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    employee_code VARCHAR(40),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_attendance_workers_name ON attendance_workers(name);
CREATE UNIQUE INDEX uq_attendance_workers_employee_code
    ON attendance_workers(upper(employee_code)) WHERE employee_code IS NOT NULL;

ALTER TABLE attendance_shifts RENAME COLUMN user_id TO worker_id;

ALTER TABLE attendance_records
    ADD COLUMN worker_id UUID REFERENCES attendance_workers(id),
    ADD COLUMN worker_name VARCHAR(120);

CREATE INDEX idx_attendance_worker_checkin
    ON attendance_records(worker_id, check_in DESC);

-- New worker punches are unique per worker. Keep any legacy open sessions
-- unique under their original user id, without deleting or rewriting rows.
DROP INDEX uq_attendance_one_open_session_per_user;
CREATE UNIQUE INDEX uq_attendance_one_open_session_per_worker
    ON attendance_records(worker_id)
    WHERE check_out IS NULL AND worker_id IS NOT NULL;
CREATE UNIQUE INDEX uq_attendance_one_legacy_open_session_per_user
    ON attendance_records(user_id)
    WHERE check_out IS NULL AND worker_id IS NULL;
