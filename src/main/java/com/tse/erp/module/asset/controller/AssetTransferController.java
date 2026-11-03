package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetTransfer;
import com.tse.erp.module.asset.service.AssetTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-transfers")
@RequiredArgsConstructor
public class AssetTransferController {

    private final AssetTransferService service;

    // Fixed path age, /{id} er upore
    @GetMapping("/current")
    public ResponseEntity<AssetTransfer> getCurrent(
            @RequestParam Long itemId) {
        AssetTransfer cur = service.getCurrent(itemId);
        return cur == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(cur);
    }

    @GetMapping
    public ResponseEntity<List<AssetTransfer>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long itemId) {
        return ResponseEntity.ok(service.getAll(buId, itemId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetTransfer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetTransfer> create(
            @RequestBody AssetTransfer r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}