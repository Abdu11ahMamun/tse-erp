package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetAudit;
import com.tse.erp.module.asset.service.AssetAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-audits")
@RequiredArgsConstructor
public class AssetAuditController {

    private final AssetAuditService service;

    @GetMapping
    public ResponseEntity<List<AssetAudit>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String disposalStatus) {
        return ResponseEntity.ok(service.getAll(buId, disposalStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetAudit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetAudit> create(@RequestBody AssetAudit r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetAudit> update(
            @PathVariable Long id, @RequestBody AssetAudit r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<AssetAudit> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<AssetAudit> approve(
            @PathVariable Long id,
            @RequestParam(required = false) Long approvedBy) {
        return ResponseEntity.ok(service.approve(id, approvedBy));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<AssetAudit> reject(@PathVariable Long id) {
        return ResponseEntity.ok(service.reject(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AssetAudit> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AssetAudit> complete(@PathVariable Long id) {
        return ResponseEntity.ok(service.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}