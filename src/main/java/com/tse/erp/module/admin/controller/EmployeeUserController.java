package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.dto.EmployeeUserCreateDto;
import com.tse.erp.module.admin.dto.EmployeeUserDto;
import com.tse.erp.module.admin.service.EmployeeUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/user")
@RequiredArgsConstructor
public class EmployeeUserController {

    private final EmployeeUserService employeeUserService;

    @GetMapping
    public ResponseEntity<EmployeeUserDto> get(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeUserService.getByEmployee(employeeId));
    }

    @PostMapping
    public ResponseEntity<EmployeeUserDto> create(
            @PathVariable Long employeeId,
            @RequestBody EmployeeUserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeUserService.createForEmployee(employeeId, dto));
    }
}