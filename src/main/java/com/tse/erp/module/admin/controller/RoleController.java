package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.dto.AssignPermissionRequestDto;
import com.tse.erp.module.admin.dto.PermissionDto;
import com.tse.erp.module.admin.dto.RoleDetailResponseDto;
import com.tse.erp.module.admin.entity.Role;
import com.tse.erp.module.admin.service.RoleDetailService;
import com.tse.erp.module.admin.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;
    private final RoleDetailService roleDetailService;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.createRole(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(
            @PathVariable Long id,
            @RequestBody Role role) {
        return ResponseEntity.ok(roleService.updateRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }


        @GetMapping("/{roleId}/details")
        public ResponseEntity<RoleDetailResponseDto>
        getRoleWithGroupedPermissions(
                @PathVariable Long roleId) {
            return ResponseEntity.ok(
                    roleDetailService
                            .getRoleWithGroupedPermissions(roleId));
        }

        @GetMapping("/{roleId}/available-permissions")
        public ResponseEntity<List<PermissionDto>>
        getAvailablePermissions(
                @PathVariable Long roleId,
                @RequestParam Long moduleId,
                @RequestParam Long menuId) {
            return ResponseEntity.ok(
                    roleDetailService.getAvailablePermissions(
                            roleId, moduleId, menuId));
        }

        @PostMapping("/{roleId}/permissions")
        public ResponseEntity<RoleDetailResponseDto>
        assignPermissions(
                @PathVariable Long roleId,
                @Valid @RequestBody
                AssignPermissionRequestDto request) {
            return ResponseEntity.ok(
                    roleDetailService.assignPermissions(
                            roleId, request));
        }
}