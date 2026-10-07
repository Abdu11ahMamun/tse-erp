package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.DepartmentGroup;
import com.tse.erp.module.hr.service.DepartmentGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/department-groups")
@RequiredArgsConstructor
public class DepartmentGroupController {

    private final DepartmentGroupService departmentGroupService;

    @GetMapping
    public ResponseEntity<List<DepartmentGroup>> getAllDepartmentGroups(
            @RequestParam(required = false) Long deptId) {
        return ResponseEntity.ok(
                departmentGroupService.getAllDepartmentGroups(deptId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentGroup> getDepartmentGroupById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                departmentGroupService.getDepartmentGroupById(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentGroup> createDepartmentGroup(
            @RequestBody DepartmentGroup departmentGroup) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentGroupService
                        .createDepartmentGroup(departmentGroup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentGroup> updateDepartmentGroup(
            @PathVariable Long id,
            @RequestBody DepartmentGroup departmentGroup) {
        return ResponseEntity.ok(
                departmentGroupService
                        .updateDepartmentGroup(id, departmentGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartmentGroup(
            @PathVariable Long id) {
        departmentGroupService.deleteDepartmentGroup(id);
        return ResponseEntity.noContent().build();
    }
}
