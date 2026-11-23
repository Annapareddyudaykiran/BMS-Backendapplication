CREATE TABLE attendance_shifts (
    user_id UUID PRIMARY KEY,
    shift_start TIME NOT NULL,
    shift_end TIME NOT NULL,
    zone_id VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT attendance_shift_times_differ CHECK (shift_start <> shift_end)
);

CREATE INDEX idx_attendance_shifts_zone ON attendance_shifts(zone_id);
