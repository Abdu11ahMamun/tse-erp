package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetAssign;
import com.tse.erp.module.asset.service.AssetAssignService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-assignments")
@RequiredArgsConstructor
public class AssetAssignController {

    private final AssetAssignService service;

    @GetMapping
    public ResponseEntity<List<AssetAssign>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long emplId,
            @RequestParam(defaultValue = "false") boolean openOnly) {
        return ResponseEntity.ok(service.getAll(buId, emplId, openOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetAssign> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<AssetAssign> create(
            @RequestBody AssetAssign r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetAssign> update(
            @PathVariable Long id, @RequestBody AssetAssign r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}