package com.microbms.attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant; import java.util.*;
public interface AttendanceRepository extends JpaRepository<AttendanceRecord,UUID>{
 Optional<AttendanceRecord> findFirstByWorkerIdAndCheckOutIsNullOrderByCheckInDesc(UUID workerId);
 List<AttendanceRecord> findByWorkerIdAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInDesc(UUID workerId,Instant from,Instant to);
 Optional<AttendanceRecord> findFirstByWorkerIdAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInDesc(UUID workerId,Instant from,Instant to);
 Optional<AttendanceRecord> findFirstByUserIdAndCheckOutIsNullOrderByCheckInDesc(UUID userId);
 List<AttendanceRecord> findByUserIdAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInDesc(UUID userId,Instant from,Instant to);
 Optional<AttendanceRecord> findFirstByUserIdAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInDesc(UUID userId,Instant from,Instant to);
 Optional<AttendanceRecord> findFirstByUserIdOrderByCheckInDesc(UUID userId);
}
