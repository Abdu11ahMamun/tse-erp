package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetUom;
import com.tse.erp.module.asset.service.AssetUomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-uoms")
@RequiredArgsConstructor
public class AssetUomController {

    private final AssetUomService assetUomService;

    @GetMapping
    public ResponseEntity<List<AssetUom>> getAllAssetUoms(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(assetUomService.getAllAssetUoms(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetUom> getAssetUomById(
            @PathVariable Long id) {
        return ResponseEntity.ok(assetUomService.getAssetUomById(id));
    }

    @PostMapping
    public ResponseEntity<AssetUom> createAssetUom(
            @RequestBody AssetUom assetUom) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetUomService.createAssetUom(assetUom));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetUom> updateAssetUom(
            @PathVariable Long id,
            @RequestBody AssetUom assetUom) {
        return ResponseEntity.ok(
                assetUomService.updateAssetUom(id, assetUom));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetUom(
            @PathVariable Long id) {
        assetUomService.deleteAssetUom(id);
        return ResponseEntity.noContent().build();
    }
}
