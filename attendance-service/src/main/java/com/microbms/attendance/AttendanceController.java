package com.microbms.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceRepository records;
    private final AttendanceShiftRepository shifts;
    private final WorkerRepository workers;
    private final Clock clock;
    private final LocalTime defaultShiftStart;
    private final LocalTime defaultShiftEnd;
    private final String defaultZoneId;
    private final String adminEmail;

    public AttendanceController(AttendanceRepository records, AttendanceShiftRepository shifts,
                                WorkerRepository workers, Clock clock,
                                @Value("${app.attendance.default-shift-start}") LocalTime defaultShiftStart,
                                @Value("${app.attendance.default-shift-end}") LocalTime defaultShiftEnd,
                                @Value("${app.attendance.zone-id}") String defaultZoneId,
                                @Value("${app.attendance.admin-email}") String adminEmail) {
        if (defaultShiftStart.equals(defaultShiftEnd)) throw new IllegalStateException("Default shift start and end must differ");
        ZoneId.of(defaultZoneId);
        this.records = records;
        this.shifts = shifts;
        this.workers = workers;
        this.clock = clock;
        this.defaultShiftStart = defaultShiftStart;
        this.defaultShiftEnd = defaultShiftEnd;
        this.defaultZoneId = defaultZoneId;
        this.adminEmail = adminEmail.trim().toLowerCase();
    }

    public record WorkerRequest(@NotBlank @Size(max = 120) String name,
                                @Size(max = 40) String employeeCode) { }
    public record WorkerView(UUID id, String name, String employeeCode, boolean active) {
        static WorkerView from(Worker worker) {
            return new WorkerView(worker.getId(), worker.getName(), worker.getEmployeeCode(), worker.isActive());
        }
    }
    public record PunchRequest(UUID workerId) { }
    public record ShiftView(LocalTime shiftStart, LocalTime shiftEnd, String zoneId) {
        static ShiftView from(AttendanceShift shift) {
            return new ShiftView(shift.getShiftStart(), shift.getShiftEnd(), shift.getZoneId());
        }
    }
    public record AttendanceView(UUID id, UUID workerId, String workerName, Instant checkIn, Instant checkOut,
                                 LocalTime scheduledStart, LocalTime scheduledEnd, String shiftZoneId,
                                 boolean lateEntry, int lateEntryMinutes,
                                 boolean earlyExit, int earlyExitMinutes, String attendanceStatus) {
        static AttendanceView from(AttendanceRecord record) {
            return new AttendanceView(record.getId(), record.getWorkerId(), record.getWorkerName(),
                    record.getCheckIn(), record.getCheckOut(), record.getScheduledStart(), record.getScheduledEnd(),
                    record.getShiftZoneId(), record.isLateEntry(), record.getLateEntryMinutes(),
                    record.isEarlyExit(), record.getEarlyExitMinutes(), record.getAttendanceStatus());
        }
    }
    public record StatusView(boolean checkedIn, ShiftView shift, AttendanceView attendance) { }

    @GetMapping("/workers")
    public List<WorkerView> workerList(@RequestAttribute("authenticatedUserEmail") String actorEmail) {
        requireAdmin(actorEmail);
        return workers.findAllByOrderByNameAsc().stream().map(WorkerView::from).toList();
    }

    @PostMapping("/workers")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerView createWorker(@RequestAttribute("authenticatedUserEmail") String actorEmail,
                                   @Valid @RequestBody WorkerRequest request) {
        requireAdmin(actorEmail);
        String code = request.employeeCode() == null || request.employeeCode().isBlank()
                ? null : request.employeeCode().trim().toUpperCase();
        if (code != null && workers.findByEmployeeCodeIgnoreCase(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A worker with this employee code already exists");
        }
        return WorkerView.from(workers.save(new Worker(request.name().trim(), code)));
    }

    @DeleteMapping("/workers/{workerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deactivateWorker(@RequestAttribute("authenticatedUserEmail") String actorEmail, @PathVariable UUID workerId) {
        requireAdmin(actorEmail);
        Worker worker = activeWorker(workerId);
        if (records.findFirstByWorkerIdAndCheckOutIsNullOrderByCheckInDesc(workerId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check out this worker before deactivating them");
        }
        worker.deactivate();
    }

    @GetMapping("/shift")
    public ShiftView shift(@RequestAttribute("authenticatedUserEmail") String actorEmail, @RequestParam UUID workerId) {
        requireAdmin(actorEmail);
        return ShiftView.from(assignedShift(findWorker(workerId)));
    }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AttendanceView checkIn(@RequestAttribute("authenticatedUserId") UUID actorId,
                                  @RequestAttribute("authenticatedUserEmail") String actorEmail,
                                  @Valid @RequestBody PunchRequest request) {
        requireAdmin(actorEmail);
        Worker worker = activeWorker(request.workerId());
        if (records.findFirstByWorkerIdAndCheckOutIsNullOrderByCheckInDesc(worker.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, worker.getName() + " is already checked in");
        }
        AttendanceShift shift = assignedShift(worker);
        return AttendanceView.from(records.save(new AttendanceRecord(actorId, worker, shift, Instant.now(clock))));
    }

    @PostMapping("/check-out")
    @Transactional
    public AttendanceView checkOut(@RequestAttribute("authenticatedUserEmail") String actorEmail,
                                   @Valid @RequestBody PunchRequest request) {
        requireAdmin(actorEmail);
        Worker worker = activeWorker(request.workerId());
        AttendanceRecord record = records.findFirstByWorkerIdAndCheckOutIsNullOrderByCheckInDesc(worker.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, worker.getName() + " has no active check-in"));
        try {
            record.checkOut(Instant.now(clock));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return AttendanceView.from(records.save(record));
    }

    @GetMapping
    public List<AttendanceView> history(@RequestAttribute("authenticatedUserEmail") String actorEmail,
                                        @RequestParam UUID workerId,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        requireAdmin(actorEmail);
        Worker worker = findWorker(workerId);
        ZoneId zone = ZoneId.of(assignedShift(worker).getZoneId());
        LocalDate today = LocalDate.now(clock.withZone(zone));
        LocalDate first = from == null ? (to == null ? today.minusDays(30) : to.minusDays(30)) : from;
        LocalDate last = to == null ? today : to;
        if (first.isAfter(last)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        Instant start = first.atStartOfDay(zone).toInstant();
        Instant end = last.plusDays(1).atStartOfDay(zone).toInstant();
        return records.findByWorkerIdAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInDesc(workerId, start, end)
                .stream().map(AttendanceView::from).toList();
    }

    @GetMapping("/status")
    @Transactional
    public StatusView status(@RequestAttribute("authenticatedUserEmail") String actorEmail, @RequestParam UUID workerId) {
        requireAdmin(actorEmail);
        Worker worker = findWorker(workerId);
        AttendanceShift shift = assignedShift(worker);
        ZoneId zone = ZoneId.of(shift.getZoneId());
        LocalDate today = LocalDate.now(clock.withZone(zone));
        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        var open = records.findFirstByWorkerIdAndCheckOutIsNullOrderByCheckInDesc(workerId);
        var current = open.isPresent() ? open : records.findFirstByWorkerIdAndCheckInGreaterThanEqualAndCheckInLessThanOrderByCheckInDesc(workerId, start, end);
        return new StatusView(open.isPresent(), ShiftView.from(shift), current.map(AttendanceView::from).orElse(null));
    }

    private void requireAdmin(String email) {
        if (email == null || !adminEmail.equals(email.trim().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the attendance administrator can manage workers and punches");
        }
    }

    private Worker activeWorker(UUID workerId) {
        Worker worker = findWorker(workerId);
        if (!worker.isActive()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker is inactive");
        return worker;
    }

    private Worker findWorker(UUID workerId) {
        if (workerId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a worker first");
        return workers.findById(workerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
    }

    private AttendanceShift assignedShift(Worker worker) {
        shifts.assignDefaultIfMissing(worker.getId(), defaultShiftStart, defaultShiftEnd, defaultZoneId);
        return shifts.findById(worker.getId()).orElseThrow(() -> new IllegalStateException("Could not load the worker's assigned shift"));
    }
}
