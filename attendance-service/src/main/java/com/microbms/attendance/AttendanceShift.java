package com.microbms.attendance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_shifts")
public class AttendanceShift {
    @Id
    @Column(name = "worker_id", nullable = false, updatable = false)
    private UUID workerId;

    @Column(name = "shift_start", nullable = false)
    private LocalTime shiftStart;

    @Column(name = "shift_end", nullable = false)
    private LocalTime shiftEnd;

    @Column(name = "zone_id", nullable = false, length = 64)
    private String zoneId;

    protected AttendanceShift() { }

    public AttendanceShift(UUID workerId, LocalTime shiftStart, LocalTime shiftEnd, String zoneId) {
        this.workerId = workerId;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.zoneId = zoneId;
    }

    public UUID getWorkerId() { return workerId; }
    public LocalTime getShiftStart() { return shiftStart; }
    public LocalTime getShiftEnd() { return shiftEnd; }
    public String getZoneId() { return zoneId; }
}
