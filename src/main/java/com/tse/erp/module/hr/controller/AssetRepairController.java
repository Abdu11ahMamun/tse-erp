package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetRepair;
import com.tse.erp.module.asset.service.AssetRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-repairs")
@RequiredArgsConstructor
public class AssetRepairController {

    private final AssetRepairService service;

    @GetMapping
    public ResponseEntity<List<AssetRepair>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(defaultValue = "false") boolean openOnly) {
        return ResponseEntity.ok(service.getAll(buId, itemId, openOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetRepair> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetRepair> create(@RequestBody AssetRepair r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetRepair> update(
            @PathVariable Long id, @RequestBody AssetRepair r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}