package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.dto.AssetLookupValueBatchDto;
import com.tse.erp.module.asset.service.AssetLookupValueBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/asset-lookup-types/{typeId}/values", "/api/v1/lookup-types/{typeId}/values"})
@RequiredArgsConstructor
public class AssetLookupValueBatchController {

    private final AssetLookupValueBatchService lookupValueBatchService;

    @GetMapping
    public ResponseEntity<AssetLookupValueBatchDto> getByType(
            @PathVariable Long typeId) {
        return ResponseEntity.ok(
                lookupValueBatchService.getByType(typeId));
    }

    @PutMapping
    public ResponseEntity<AssetLookupValueBatchDto> saveByType(
            @PathVariable Long typeId,
            @RequestBody AssetLookupValueBatchDto dto) {
        return ResponseEntity.ok(
                lookupValueBatchService.saveByType(typeId, dto));
    }
}
