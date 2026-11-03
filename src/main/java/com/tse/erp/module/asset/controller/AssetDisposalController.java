package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetDisposal;
import com.tse.erp.module.asset.service.AssetDisposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-disposals")
@RequiredArgsConstructor
public class AssetDisposalController {

    private final AssetDisposalService service;

    @GetMapping
    public ResponseEntity<List<AssetDisposal>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String disposalStatus) {
        return ResponseEntity.ok(service.getAll(buId, disposalStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDisposal> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetDisposal> create(
            @RequestBody AssetDisposal r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDisposal> update(
            @PathVariable Long id, @RequestBody AssetDisposal r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<AssetDisposal> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<AssetDisposal> approve(
            @PathVariable Long id,
            @RequestParam(required = false) Long approvedBy) {
        return ResponseEntity.ok(service.approve(id, approvedBy));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<AssetDisposal> reject(@PathVariable Long id) {
        return ResponseEntity.ok(service.reject(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AssetDisposal> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AssetDisposal> complete(@PathVariable Long id) {
        return ResponseEntity.ok(service.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}