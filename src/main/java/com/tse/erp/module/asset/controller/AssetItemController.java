package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetItem;
import com.tse.erp.module.asset.service.AssetItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-items")
@RequiredArgsConstructor
public class AssetItemController {

    private final AssetItemService assetItemService;

    @GetMapping
    public ResponseEntity<List<AssetItem>> getAllAssetItems(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                assetItemService.getAllAssetItems(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetItem> getAssetItemById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                assetItemService.getAssetItemById(id));
    }

    @PostMapping
    public ResponseEntity<AssetItem> createAssetItem(
            @RequestBody AssetItem assetItem) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetItemService.createAssetItem(assetItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetItem> updateAssetItem(
            @PathVariable Long id,
            @RequestBody AssetItem assetItem) {
        return ResponseEntity.ok(
                assetItemService.updateAssetItem(id, assetItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetItem(
            @PathVariable Long id) {
        assetItemService.deleteAssetItem(id);
        return ResponseEntity.noContent().build();
    }
}
