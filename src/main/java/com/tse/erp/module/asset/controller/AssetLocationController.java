package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetLocation;
import com.tse.erp.module.asset.service.AssetLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-locations")
@RequiredArgsConstructor
public class AssetLocationController {

    private final AssetLocationService assetLocationService;

    @GetMapping
    public ResponseEntity<List<AssetLocation>> getAllAssetLocations(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                assetLocationService.getAllAssetLocations(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetLocation> getAssetLocationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                assetLocationService.getAssetLocationById(id));
    }

    @PostMapping
    public ResponseEntity<AssetLocation> createAssetLocation(
            @RequestBody AssetLocation assetLocation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetLocationService
                        .createAssetLocation(assetLocation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetLocation> updateAssetLocation(
            @PathVariable Long id,
            @RequestBody AssetLocation assetLocation) {
        return ResponseEntity.ok(
                assetLocationService
                        .updateAssetLocation(id, assetLocation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetLocation(
            @PathVariable Long id) {
        assetLocationService.deleteAssetLocation(id);
        return ResponseEntity.noContent().build();
    }
}
