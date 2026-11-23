ALTER TABLE attendance_records
    ADD COLUMN scheduled_start TIME NOT NULL DEFAULT TIME '09:30',
    ADD COLUMN scheduled_end TIME NOT NULL DEFAULT TIME '16:30',
    ADD COLUMN shift_zone_id VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    ADD COLUMN late_entry BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN late_entry_minutes INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN early_exit BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN early_exit_minutes INTEGER NOT NULL DEFAULT 0;

-- Preserve existing rows and calculate their attendance metrics against the
-- default shift snapshot added above.
UPDATE attendance_records
SET late_entry = (check_in AT TIME ZONE shift_zone_id)::time > scheduled_start,
    late_entry_minutes = GREATEST(0, FLOOR(EXTRACT(EPOCH FROM
        ((check_in AT TIME ZONE shift_zone_id)::time - scheduled_start)) / 60)::integer),
    early_exit = check_out IS NOT NULL AND (check_out AT TIME ZONE shift_zone_id)::time < scheduled_end,
    early_exit_minutes = CASE WHEN check_out IS NULL THEN 0 ELSE GREATEST(0, FLOOR(EXTRACT(EPOCH FROM
        (scheduled_end - (check_out AT TIME ZONE shift_zone_id)::time)) / 60)::integer) END;

ALTER TABLE attendance_records
    ADD CONSTRAINT attendance_late_minutes_nonnegative CHECK (late_entry_minutes >= 0),
    ADD CONSTRAINT attendance_early_minutes_nonnegative CHECK (early_exit_minutes >= 0);
