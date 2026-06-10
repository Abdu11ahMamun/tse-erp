package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.RoleDetail;
import com.tse.erp.module.admin.service.RoleDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-details")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleDetailController {

    private final RoleDetailService roleDetailService;

    @GetMapping
    public ResponseEntity<List<RoleDetail>> getAllRoleDetails() {
        return ResponseEntity.ok(roleDetailService.getAllRoleDetails());
    }

    // Role id diye filter
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RoleDetail>> getRoleDetailsByRole(
            @PathVariable Long roleId) {
        return ResponseEntity.ok(
                roleDetailService.getRoleDetailsByRoleId(roleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDetail> getRoleDetailById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                roleDetailService.getRoleDetailById(id));
    }

    @PostMapping
    public ResponseEntity<RoleDetail> createRoleDetail(
            @RequestBody RoleDetail roleDetail) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleDetailService.createRoleDetail(roleDetail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDetail> updateRoleDetail(
            @PathVariable Long id,
            @RequestBody RoleDetail roleDetail) {
        return ResponseEntity.ok(
                roleDetailService.updateRoleDetail(id, roleDetail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoleDetail(@PathVariable Long id) {
        roleDetailService.deleteRoleDetail(id);
        return ResponseEntity.noContent().build();
    }
}