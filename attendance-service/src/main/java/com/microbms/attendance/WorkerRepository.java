package com.microbms.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {
    List<Worker> findAllByOrderByNameAsc();
    Optional<Worker> findByEmployeeCodeIgnoreCase(String employeeCode);
}
