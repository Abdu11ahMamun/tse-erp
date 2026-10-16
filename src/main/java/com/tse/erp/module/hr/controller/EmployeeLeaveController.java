package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.dto.LeaveBalanceDto;
import com.tse.erp.module.hr.dto.LeaveCalcDto;
import com.tse.erp.module.hr.entity.EmployeeLeave;
import com.tse.erp.module.hr.service.EmployeeLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-leaves")
@RequiredArgsConstructor
public class EmployeeLeaveController {

    private final EmployeeLeaveService employeeLeaveService;

    @GetMapping("/available-categories")
    public ResponseEntity<List<LeaveBalanceDto>> getAvailableCategories(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(
                employeeLeaveService.getAvailableCategories(
                        employeeId, year));
    }

    @GetMapping("/calculate")
    public ResponseEntity<LeaveCalcDto> calculate(
            @RequestParam Long employeeId,
            @RequestParam Long leaveCategoryId,
            @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(
                employeeLeaveService.calculate(
                        employeeId, leaveCategoryId, fromDate, toDate));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeLeave>> getAllEmployeeLeaves(
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(
                employeeLeaveService.getAllEmployeeLeaves(
                        view, buId, employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeLeave> getEmployeeLeaveById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                employeeLeaveService.getEmployeeLeaveById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeLeave> createEmployeeLeave(
            @RequestBody EmployeeLeave row,
            @RequestParam(defaultValue = "false") boolean submit) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeLeaveService.createEmployeeLeave(row, submit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeLeave> updateEmployeeLeave(
            @PathVariable Long id,
            @RequestBody EmployeeLeave row,
            @RequestParam(defaultValue = "false") boolean submit) {
        return ResponseEntity.ok(
                employeeLeaveService.updateEmployeeLeave(id, row, submit));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<EmployeeLeave> submitEmployeeLeave(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                employeeLeaveService.submitEmployeeLeave(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployeeLeave(
            @PathVariable Long id) {
        employeeLeaveService.deleteEmployeeLeave(id);
        return ResponseEntity.noContent().build();
    }
}
