package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.AssignWarehouseEmployeesRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseEmployeeMapDto;
import com.tse.erp.module.inventory.service.InvItemWarehouseEmployeeMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/inv-item-warehouse-employee-maps", "/api/v1/warehouse-employee-maps"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemWarehouseEmployeeMapController {

    private final InvItemWarehouseEmployeeMapService warehouseEmployeeMapService;

    @GetMapping
    public ResponseEntity<List<InvItemWarehouseEmployeeMapDto>> getEmployeesByWarehouseId(
            @RequestParam(required = false) Long warehouseId) {
        if (warehouseId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(warehouseEmployeeMapService.getEmployeesByWarehouseId(warehouseId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InvItemWarehouseEmployeeMapDto>> getEmployeesByWarehouseIdPath(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(warehouseEmployeeMapService.getEmployeesByWarehouseId(warehouseId));
    }

    @PostMapping
    public ResponseEntity<List<InvItemWarehouseEmployeeMapDto>> assignEmployeesToWarehouse(
            @RequestBody AssignWarehouseEmployeesRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(warehouseEmployeeMapService.assignEmployeesToWarehouse(request));
    }

    @PostMapping("/assign")
    public ResponseEntity<List<InvItemWarehouseEmployeeMapDto>> assignEmployeesToWarehouseSubpath(
            @RequestBody AssignWarehouseEmployeesRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(warehouseEmployeeMapService.assignEmployeesToWarehouse(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployeeMap(@PathVariable Long id) {
        warehouseEmployeeMapService.deleteEmployeeMap(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/warehouse/{warehouseId}/employee/{employeeId}")
    public ResponseEntity<Void> removeEmployeeFromWarehouse(
            @PathVariable Long warehouseId,
            @PathVariable Long employeeId) {
        warehouseEmployeeMapService.removeEmployeeFromWarehouse(warehouseId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvItemWarehouseEmployeeMapDto> updateMapStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
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
        return ResponseEntity.ok(warehouseEmployeeMapService.updateMapStatus(id, resolvedStatus));
    }
}
