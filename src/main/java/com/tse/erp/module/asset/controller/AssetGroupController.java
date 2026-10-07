package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetGroup;
import com.tse.erp.module.asset.service.AssetGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-groups")
@RequiredArgsConstructor
public class AssetGroupController {

    private final AssetGroupService assetGroupService;

    @GetMapping
    public ResponseEntity<List<AssetGroup>> getAllAssetGroups(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(assetGroupService.getAllAssetGroups(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetGroup> getAssetGroupById(
            @PathVariable Long id) {
        return ResponseEntity.ok(assetGroupService.getAssetGroupById(id));
    }

    @PostMapping
    public ResponseEntity<AssetGroup> createAssetGroup(
            @RequestBody AssetGroup assetGroup) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetGroupService.createAssetGroup(assetGroup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetGroup> updateAssetGroup(
            @PathVariable Long id,
            @RequestBody AssetGroup assetGroup) {
        return ResponseEntity.ok(
                assetGroupService.updateAssetGroup(id, assetGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetGroup(
            @PathVariable Long id) {
        assetGroupService.deleteAssetGroup(id);
        return ResponseEntity.noContent().build();
    }
}
