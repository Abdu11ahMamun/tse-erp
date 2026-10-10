package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.EmployeeCertification;
import com.tse.erp.module.hr.entity.EmployeeEducation;
import com.tse.erp.module.hr.entity.EmployeeJobExperience;
import com.tse.erp.module.hr.entity.EmployeeTraining;
import com.tse.erp.module.hr.service.EmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    @GetMapping("/educations")
    public ResponseEntity<List<EmployeeEducation>> getEducations(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                employeeProfileService.getEducations(employeeId));
    }

    @PutMapping("/educations")
    public ResponseEntity<List<EmployeeEducation>> replaceEducations(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeEducation> rows) {
        return ResponseEntity.ok(
                employeeProfileService.replaceEducations(employeeId, rows));
    }

    @GetMapping("/trainings")
    public ResponseEntity<List<EmployeeTraining>> getTrainings(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                employeeProfileService.getTrainings(employeeId));
    }

    @PutMapping("/trainings")
    public ResponseEntity<List<EmployeeTraining>> replaceTrainings(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeTraining> rows) {
        return ResponseEntity.ok(
                employeeProfileService.replaceTrainings(employeeId, rows));
    }

    @GetMapping("/certifications")
    public ResponseEntity<List<EmployeeCertification>> getCertifications(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                employeeProfileService.getCertifications(employeeId));
    }

    @PutMapping("/certifications")
    public ResponseEntity<List<EmployeeCertification>> replaceCertifications(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeCertification> rows) {
        return ResponseEntity.ok(
                employeeProfileService.replaceCertifications(employeeId, rows));
    }

    @GetMapping("/job-experiences")
    public ResponseEntity<List<EmployeeJobExperience>> getJobExperiences(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                employeeProfileService.getJobExperiences(employeeId));
    }

    @PutMapping("/job-experiences")
    public ResponseEntity<List<EmployeeJobExperience>> replaceJobExperiences(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeJobExperience> rows) {
        return ResponseEntity.ok(
                employeeProfileService.replaceJobExperiences(
                        employeeId, rows));
    }
}
