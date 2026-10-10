package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.EmploymentType;
import com.tse.erp.module.hr.service.EmploymentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employment-types")
@RequiredArgsConstructor
public class EmploymentTypeController {

    private final EmploymentTypeService employmentTypeService;

    @GetMapping
    public ResponseEntity<List<EmploymentType>> getAllEmploymentTypes() {
        return ResponseEntity.ok(
                employmentTypeService.getAllEmploymentTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmploymentType> getEmploymentTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                employmentTypeService.getEmploymentTypeById(id));
    }

    @PostMapping
    public ResponseEntity<EmploymentType> createEmploymentType(
            @RequestBody EmploymentType employmentType) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employmentTypeService
                        .createEmploymentType(employmentType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmploymentType> updateEmploymentType(
            @PathVariable Long id,
            @RequestBody EmploymentType employmentType) {
        return ResponseEntity.ok(
                employmentTypeService
                        .updateEmploymentType(id, employmentType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmploymentType(
            @PathVariable Long id) {
        employmentTypeService.deleteEmploymentType(id);
        return ResponseEntity.noContent().build();
    }
}
