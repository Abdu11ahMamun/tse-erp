package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetItemSpec;
import com.tse.erp.module.asset.service.AssetItemSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-item-specs")
@RequiredArgsConstructor
public class AssetItemSpecController {

    private final AssetItemSpecService assetItemSpecService;

    @GetMapping
    public ResponseEntity<List<AssetItemSpec>> getAllAssetItemSpecs(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long itemNameId) {
        return ResponseEntity.ok(
                assetItemSpecService
                        .getAllAssetItemSpecs(buId, itemNameId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetItemSpec> getAssetItemSpecById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                assetItemSpecService.getAssetItemSpecById(id));
    }

    @PostMapping
    public ResponseEntity<AssetItemSpec> createAssetItemSpec(
            @RequestBody AssetItemSpec assetItemSpec) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetItemSpecService
                        .createAssetItemSpec(assetItemSpec));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetItemSpec> updateAssetItemSpec(
            @PathVariable Long id,
            @RequestBody AssetItemSpec assetItemSpec) {
        return ResponseEntity.ok(
                assetItemSpecService
                        .updateAssetItemSpec(id, assetItemSpec));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetItemSpec(
            @PathVariable Long id) {
        assetItemSpecService.deleteAssetItemSpec(id);
        return ResponseEntity.noContent().build();
    }
}
