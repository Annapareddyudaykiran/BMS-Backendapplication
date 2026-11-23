package com.microbms.attendance;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceRecordTest {
    private static final AttendanceShift SHIFT = new AttendanceShift(
            UUID.fromString("93865f1c-d891-45f8-aee8-59b777a5f20f"),
            LocalTime.of(9, 30), LocalTime.of(16, 30), "Asia/Kolkata");

    @Test
    void recordsTwentyMinuteLateEntryAndThirtyMinuteEarlyExit() {
        // 09:50 and 16:00 in Asia/Kolkata, represented as UTC instants.
        AttendanceRecord record = new AttendanceRecord(SHIFT.getWorkerId(), SHIFT, Instant.parse("2026-11-20T04:20:00Z"));
        record.checkOut(Instant.parse("2026-11-20T10:30:00Z"));

        assertTrue(record.isLateEntry());
        assertEquals(20, record.getLateEntryMinutes());
        assertTrue(record.isEarlyExit());
        assertEquals(30, record.getEarlyExitMinutes());
        assertEquals("Late Entry · Early Exit", record.getAttendanceStatus());
    }

    @Test
    void recordsPresentForOnTimeAttendance() {
        // 09:30 and 16:30 in Asia/Kolkata, represented as UTC instants.
        AttendanceRecord record = new AttendanceRecord(SHIFT.getWorkerId(), SHIFT, Instant.parse("2026-11-20T04:00:00Z"));
        record.checkOut(Instant.parse("2026-11-20T11:00:00Z"));

        assertFalse(record.isLateEntry());
        assertEquals(0, record.getLateEntryMinutes());
        assertFalse(record.isEarlyExit());
        assertEquals(0, record.getEarlyExitMinutes());
        assertEquals("Present", record.getAttendanceStatus());
    }
}
