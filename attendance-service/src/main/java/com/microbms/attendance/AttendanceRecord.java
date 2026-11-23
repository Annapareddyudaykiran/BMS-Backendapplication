package com.microbms.attendance;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
@Entity @Table(name="attendance_records",indexes={@Index(name="idx_attendance_user_checkin",columnList="user_id,check_in")})
public class AttendanceRecord {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="user_id",nullable=false,updatable=false) private UUID userId;
 @Column(name="worker_id",updatable=false) private UUID workerId;
 @Column(name="worker_name",updatable=false,length=120) private String workerName;
 @Column(name="check_in",nullable=false,updatable=false) private Instant checkIn;
 @Column(name="check_out") private Instant checkOut;
 @Column(name="scheduled_start",nullable=false,updatable=false) private LocalTime scheduledStart;
 @Column(name="scheduled_end",nullable=false,updatable=false) private LocalTime scheduledEnd;
 @Column(name="shift_zone_id",nullable=false,updatable=false,length=64) private String shiftZoneId;
 @Column(name="late_entry",nullable=false) private boolean lateEntry;
 @Column(name="late_entry_minutes",nullable=false) private int lateEntryMinutes;
 @Column(name="early_exit",nullable=false) private boolean earlyExit;
 @Column(name="early_exit_minutes",nullable=false) private int earlyExitMinutes;
 protected AttendanceRecord(){}
 public AttendanceRecord(UUID userId, AttendanceShift shift, Instant checkIn){
  this.userId=userId;this.checkIn=checkIn;this.scheduledStart=shift.getShiftStart();this.scheduledEnd=shift.getShiftEnd();this.shiftZoneId=shift.getZoneId();
  long minutes=Duration.between(scheduledStart,checkIn.atZone(ZoneId.of(shiftZoneId)).toLocalTime()).toMinutes();
  this.lateEntryMinutes=(int)Math.max(0,minutes);this.lateEntry=this.lateEntryMinutes>0;
 }
 public AttendanceRecord(UUID actorId, Worker worker, AttendanceShift shift, Instant checkIn){
  this(actorId, shift, checkIn); this.workerId=worker.getId(); this.workerName=worker.getName();
 }
 public UUID getId(){return id;} public UUID getUserId(){return userId;} public UUID getWorkerId(){return workerId;} public String getWorkerName(){return workerName;} public Instant getCheckIn(){return checkIn;} public Instant getCheckOut(){return checkOut;}
 public LocalTime getScheduledStart(){return scheduledStart;} public LocalTime getScheduledEnd(){return scheduledEnd;} public String getShiftZoneId(){return shiftZoneId;}
 public boolean isLateEntry(){return lateEntry;} public int getLateEntryMinutes(){return lateEntryMinutes;} public boolean isEarlyExit(){return earlyExit;} public int getEarlyExitMinutes(){return earlyExitMinutes;}
 public void checkOut(Instant at){if(at.isBefore(checkIn))throw new IllegalArgumentException("Check-out cannot be before check-in");this.checkOut=at;long minutes=Duration.between(at.atZone(ZoneId.of(shiftZoneId)).toLocalTime(),scheduledEnd).toMinutes();this.earlyExitMinutes=(int)Math.max(0,minutes);this.earlyExit=this.earlyExitMinutes>0;}
 public String getAttendanceStatus(){if(lateEntry&&earlyExit)return "Late Entry · Early Exit";if(lateEntry)return "Late Entry";if(earlyExit)return "Early Exit";return "Present";}
}
