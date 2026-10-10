package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.LeaveType;
import com.tse.erp.module.hr.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes(
            @RequestParam(required = false) Long bgId) {
        return ResponseEntity.ok(
                leaveTypeService.getAllLeaveTypes(bgId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveType> getLeaveTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                leaveTypeService.getLeaveTypeById(id));
    }

    @PostMapping
    public ResponseEntity<LeaveType> createLeaveType(
            @RequestBody LeaveType leaveType) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveTypeService.createLeaveType(leaveType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveType> updateLeaveType(
            @PathVariable Long id,
            @RequestBody LeaveType leaveType) {
        return ResponseEntity.ok(
                leaveTypeService.updateLeaveType(id, leaveType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveType(
            @PathVariable Long id) {
        leaveTypeService.deleteLeaveType(id);
        return ResponseEntity.noContent().build();
    }
}
