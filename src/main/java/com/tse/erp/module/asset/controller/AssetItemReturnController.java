package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetItemReturn;
import com.tse.erp.module.asset.service.AssetItemReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-returns")
@RequiredArgsConstructor
public class AssetItemReturnController {

    private final AssetItemReturnService service;

    @GetMapping
    public ResponseEntity<List<AssetItemReturn>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long emplId) {
        return ResponseEntity.ok(service.getAll(buId, emplId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetItemReturn> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetItemReturn> create(
            @RequestBody AssetItemReturn r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetItemReturn> update(
            @PathVariable Long id, @RequestBody AssetItemReturn r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}