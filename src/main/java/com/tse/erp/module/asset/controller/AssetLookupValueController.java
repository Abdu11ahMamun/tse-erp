package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.AssetLookupValue;
import com.tse.erp.module.asset.service.AssetLookupValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/asset-lookup-values", "/api/v1/lookup-values"})
@RequiredArgsConstructor
public class AssetLookupValueController {

    private final AssetLookupValueService lookupValueService;

    @GetMapping
    public ResponseEntity<List<AssetLookupValue>> getAllLookupValues(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long typeId) {
        return ResponseEntity.ok(
                lookupValueService.getAllLookupValues(buId, typeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetLookupValue> getLookupValueById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupValueService.getLookupValueById(id));
    }

    @PostMapping
    public ResponseEntity<AssetLookupValue> createLookupValue(
            @RequestBody AssetLookupValue lookupValue) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupValueService.createLookupValue(lookupValue));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetLookupValue> updateLookupValue(
            @PathVariable Long id,
            @RequestBody AssetLookupValue lookupValue) {
        return ResponseEntity.ok(
                lookupValueService.updateLookupValue(id, lookupValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLookupValue(
            @PathVariable Long id) {
        lookupValueService.deleteLookupValue(id);
        return ResponseEntity.noContent().build();
    }
}
