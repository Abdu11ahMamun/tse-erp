package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.dto.AssetBookRegisterDto;
import com.tse.erp.module.asset.service.AssetBookRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-book-registers")
@RequiredArgsConstructor
public class AssetBookRegisterController {

    private final AssetBookRegisterService service;

    @GetMapping
    public ResponseEntity<List<AssetBookRegisterDto>> getAll(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(service.getAllAssetBookRegisters(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetBookRegisterDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAssetBookRegisterById(id));
    }

    @PostMapping
    public ResponseEntity<AssetBookRegisterDto> create(
            @RequestBody AssetBookRegisterDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createAssetBookRegister(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetBookRegisterDto> update(
            @PathVariable Long id,
            @RequestBody AssetBookRegisterDto dto) {
        return ResponseEntity.ok(service.updateAssetBookRegister(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AssetBookRegisterDto> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return ResponseEntity.ok(service.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteAssetBookRegister(id);
        return ResponseEntity.noContent().build();
    }
}
