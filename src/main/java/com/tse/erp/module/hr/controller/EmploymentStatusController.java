package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.EmploymentStatus;
import com.tse.erp.module.hr.service.EmploymentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employment-statuses")
@RequiredArgsConstructor
public class EmploymentStatusController {

    private final EmploymentStatusService employmentStatusService;

    @GetMapping
    public ResponseEntity<List<EmploymentStatus>> getAllEmploymentStatuses() {
        return ResponseEntity.ok(
                employmentStatusService.getAllEmploymentStatuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmploymentStatus> getEmploymentStatusById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                employmentStatusService.getEmploymentStatusById(id));
    }

    @PostMapping
    public ResponseEntity<EmploymentStatus> createEmploymentStatus(
            @RequestBody EmploymentStatus employmentStatus) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employmentStatusService
                        .createEmploymentStatus(employmentStatus));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmploymentStatus> updateEmploymentStatus(
            @PathVariable Long id,
            @RequestBody EmploymentStatus employmentStatus) {
        return ResponseEntity.ok(
                employmentStatusService
                        .updateEmploymentStatus(id, employmentStatus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmploymentStatus(
            @PathVariable Long id) {
        employmentStatusService.deleteEmploymentStatus(id);
        return ResponseEntity.noContent().build();
    }
}