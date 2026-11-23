CREATE TABLE attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    check_in TIMESTAMPTZ NOT NULL DEFAULT now(),
    check_out TIMESTAMPTZ,
    CONSTRAINT attendance_checkout_after_checkin CHECK (check_out IS NULL OR check_out >= check_in)
);
CREATE INDEX idx_attendance_user_checkin ON attendance_records(user_id, check_in DESC);
CREATE UNIQUE INDEX uq_attendance_one_open_session_per_user ON attendance_records(user_id) WHERE check_out IS NULL;
