package com.microbms.attendance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_workers")
public class Worker {
    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "employee_code", length = 40)
    private String employeeCode;

    @Column(name = "owner_email", nullable = false, length = 254)
    private String ownerEmail;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Worker() { }

    public Worker(String name, String employeeCode, String ownerEmail) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.employeeCode = employeeCode;
        this.ownerEmail = ownerEmail;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmployeeCode() { return employeeCode; }
    public String getOwnerEmail() { return ownerEmail; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    public void deactivate() { this.active = false; }
}
