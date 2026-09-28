package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.dto.AssignRoleRequestDto;
import com.tse.erp.module.admin.dto.BuRoleMapResponseDto;
import com.tse.erp.module.admin.service.BuRoleMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business-units")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BuRoleMapController {

    private final BuRoleMapService buRoleMapService;

    // GET all roles assigned to a business unit
    @GetMapping("/{buId}/roles")
    public ResponseEntity<BuRoleMapResponseDto> getRolesByBu(
            @PathVariable Long buId) {
        return ResponseEntity.ok(
                buRoleMapService.getRolesByBusinessUnit(buId));
    }

    // POST — assign a role to business unit
    @PostMapping("/{buId}/roles")
    public ResponseEntity<BuRoleMapResponseDto> assignRole(
            @PathVariable Long buId,
            @Valid @RequestBody AssignRoleRequestDto request) {
        return ResponseEntity.ok(
                buRoleMapService.assignRole(buId, request.getRoleId()));
    }

    // DELETE — remove a role mapping
    @DeleteMapping("/{buId}/roles/{mappingId}")
    public ResponseEntity<BuRoleMapResponseDto> removeRole(
            @PathVariable Long buId,
            @PathVariable Long mappingId) {
        return ResponseEntity.ok(
                buRoleMapService.removeRole(buId, mappingId));
    }
}