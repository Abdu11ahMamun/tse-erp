package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.AssignStoreEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreEmployeeMapDto;
import com.tse.erp.module.inventory.service.InvItemStoreEmployeeMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-item-store-employee-maps", "/api/v1/store-employee-maps"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemStoreEmployeeMapController {

    private final InvItemStoreEmployeeMapService storeEmployeeMapService;

    @GetMapping
    public ResponseEntity<List<InvItemStoreEmployeeMapDto>> getEmployeesByStoreId(
            @RequestParam(required = false) Long storeId) {
        if (storeId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(storeEmployeeMapService.getEmployeesByStoreId(storeId));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<InvItemStoreEmployeeMapDto>> getEmployeesByStoreIdPath(
            @PathVariable Long storeId) {
        return ResponseEntity.ok(storeEmployeeMapService.getEmployeesByStoreId(storeId));
    }

    @PostMapping
    public ResponseEntity<List<InvItemStoreEmployeeMapDto>> assignEmployeesToStore(
            @RequestBody AssignStoreEmployeesRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storeEmployeeMapService.assignEmployeesToStore(request));
    }

    @PostMapping("/assign")
    public ResponseEntity<List<InvItemStoreEmployeeMapDto>> assignEmployeesToStoreSubpath(
            @RequestBody AssignStoreEmployeesRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storeEmployeeMapService.assignEmployeesToStore(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployeeMap(@PathVariable Long id) {
        storeEmployeeMapService.deleteEmployeeMap(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/store/{storeId}/employee/{employeeId}")
    public ResponseEntity<Void> removeEmployeeFromStore(
            @PathVariable Long storeId,
            @PathVariable Long employeeId) {
        storeEmployeeMapService.removeEmployeeFromStore(storeId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvItemStoreEmployeeMapDto> updateMapStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestBody(required = false) Map<String, Object> body) {
        Integer resolvedStatus = status;
        if (resolvedStatus == null && body != null && body.containsKey("status")) {
            Object val = body.get("status");
            if (val instanceof Number n) {
                resolvedStatus = n.intValue();
            } else if (val != null) {
                try {
                    resolvedStatus = Integer.parseInt(val.toString().trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        if (resolvedStatus == null) {
            resolvedStatus = 1;
        }
        return ResponseEntity.ok(storeEmployeeMapService.updateMapStatus(id, resolvedStatus));
    }
}

