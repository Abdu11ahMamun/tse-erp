package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.Permission;
import com.tse.erp.module.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    // Module id diye filter
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<Permission>> getPermissionsByModule(
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(
                permissionService.getPermissionsByModuleId(moduleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(
            @PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(
            @RequestBody Permission permission) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permissionService.createPermission(permission));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permission> updatePermission(
            @PathVariable Long id,
            @RequestBody Permission permission) {
        return ResponseEntity.ok(
                permissionService.updatePermission(id, permission));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}