package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.dto.UserBuDto;
import com.tse.erp.module.admin.dto.UserEmployeeDto;
import com.tse.erp.module.admin.service.UserAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}")
@RequiredArgsConstructor
public class UserAssignmentController {

    private final UserAssignmentService userAssignmentService;

    @GetMapping("/employee")
    public ResponseEntity<UserEmployeeDto> getEmployee(
            @PathVariable Long userId) {
        return ResponseEntity.ok(userAssignmentService.getEmployee(userId));
    }

    @PutMapping("/employee")
    public ResponseEntity<UserEmployeeDto> saveEmployee(
            @PathVariable Long userId,
            @RequestBody UserEmployeeDto dto) {
        return ResponseEntity.ok(
                userAssignmentService.saveEmployee(userId, dto));
    }

    @GetMapping("/business-units")
    public ResponseEntity<UserBuDto> getBusinessUnits(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                userAssignmentService.getBusinessUnits(userId));
    }

    @PutMapping("/business-units")
    public ResponseEntity<UserBuDto> saveBusinessUnits(
            @PathVariable Long userId,
            @RequestBody UserBuDto dto) {
        return ResponseEntity.ok(
                userAssignmentService.saveBusinessUnits(userId, dto));
    }
}
