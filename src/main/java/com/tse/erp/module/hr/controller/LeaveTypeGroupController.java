package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.LeaveTypeGroup;
import com.tse.erp.module.hr.service.LeaveTypeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-type-groups")
@RequiredArgsConstructor
public class LeaveTypeGroupController {

    private final LeaveTypeGroupService leaveTypeGroupService;

    @GetMapping
    public ResponseEntity<List<LeaveTypeGroup>> getAllLeaveTypeGroups(
            @RequestParam(required = false) Long leaveTypeId) {
        return ResponseEntity.ok(
                leaveTypeGroupService.getAllLeaveTypeGroups(leaveTypeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeGroup> getLeaveTypeGroupById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                leaveTypeGroupService.getLeaveTypeGroupById(id));
    }

    @PostMapping
    public ResponseEntity<LeaveTypeGroup> createLeaveTypeGroup(
            @RequestBody LeaveTypeGroup leaveTypeGroup) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveTypeGroupService
                        .createLeaveTypeGroup(leaveTypeGroup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeGroup> updateLeaveTypeGroup(
            @PathVariable Long id,
            @RequestBody LeaveTypeGroup leaveTypeGroup) {
        return ResponseEntity.ok(
                leaveTypeGroupService
                        .updateLeaveTypeGroup(id, leaveTypeGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveTypeGroup(
            @PathVariable Long id) {
        leaveTypeGroupService.deleteLeaveTypeGroup(id);
        return ResponseEntity.noContent().build();
    }
}
