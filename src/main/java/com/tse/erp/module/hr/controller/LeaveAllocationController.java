package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.LeaveAllocation;
import com.tse.erp.module.hr.service.LeaveAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-allocations")
@RequiredArgsConstructor
public class LeaveAllocationController {

    private final LeaveAllocationService leaveAllocationService;

    @GetMapping
    public ResponseEntity<List<LeaveAllocation>> getAllLeaveAllocations(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer leaveYear) {
        return ResponseEntity.ok(
                leaveAllocationService.getAllLeaveAllocations(
                        buId, leaveYear));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveAllocation> getLeaveAllocationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                leaveAllocationService.getLeaveAllocationById(id));
    }

    @PostMapping
    public ResponseEntity<LeaveAllocation> createLeaveAllocation(
            @RequestBody LeaveAllocation leaveAllocation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveAllocationService
                        .createLeaveAllocation(leaveAllocation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveAllocation> updateLeaveAllocation(
            @PathVariable Long id,
            @RequestBody LeaveAllocation leaveAllocation) {
        return ResponseEntity.ok(
                leaveAllocationService.updateLeaveAllocation(
                        id, leaveAllocation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveAllocation(
            @PathVariable Long id) {
        leaveAllocationService.deleteLeaveAllocation(id);
        return ResponseEntity.noContent().build();
    }
}
