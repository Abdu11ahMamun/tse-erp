package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetMaintenance;
import com.tse.erp.module.asset.service.AssetMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-maintenances")
@RequiredArgsConstructor
public class AssetMaintenanceController {

    private final AssetMaintenanceService service;

    @GetMapping
    public ResponseEntity<List<AssetMaintenance>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long itemId) {
        return ResponseEntity.ok(service.getAll(buId, itemId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetMaintenance> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetMaintenance> create(
            @RequestBody AssetMaintenance r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetMaintenance> update(
            @PathVariable Long id, @RequestBody AssetMaintenance r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}