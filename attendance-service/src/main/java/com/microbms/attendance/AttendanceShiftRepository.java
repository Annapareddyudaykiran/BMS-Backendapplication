package com.microbms.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface AttendanceShiftRepository extends JpaRepository<AttendanceShift, UUID> {
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO attendance_shifts (worker_id, shift_start, shift_end, zone_id) " +
            "VALUES (:workerId, :shiftStart, :shiftEnd, :zoneId) ON CONFLICT (worker_id) DO NOTHING", nativeQuery = true)
    int assignDefaultIfMissing(@Param("workerId") UUID workerId,
                               @Param("shiftStart") LocalTime shiftStart,
                               @Param("shiftEnd") LocalTime shiftEnd,
                               @Param("zoneId") String zoneId);
}
