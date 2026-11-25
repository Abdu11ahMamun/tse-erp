package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.dto.AssetLookupTypeRequestDto;
import com.tse.erp.module.asset.dto.AssetLookupTypeResponseDto;
import com.tse.erp.module.asset.dto.AssetLookupValueRowDto;
import com.tse.erp.module.asset.service.AssetLookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/asset-lookup-types", "/api/v1/lookup-types"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssetLookupTypeController {

    private final AssetLookupTypeService lookupTypeService;

    @GetMapping
    public ResponseEntity<List<AssetLookupTypeResponseDto>> getAllLookupTypes(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                lookupTypeService.getAllLookupTypes(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetLookupTypeResponseDto> getLookupTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupTypeService.getLookupTypeById(id));
    }

    @GetMapping("/{id}/values")
    public ResponseEntity<List<AssetLookupValueRowDto>> getLookupValues(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupTypeService.getLookupValues(id));
    }

    @PostMapping
    public ResponseEntity<AssetLookupTypeResponseDto> createLookupType(
            @RequestBody AssetLookupTypeRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupTypeService.createLookupType(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetLookupTypeResponseDto> updateLookupType(
            @PathVariable Long id,
            @RequestBody AssetLookupTypeRequestDto request) {
        return ResponseEntity.ok(
                lookupTypeService.updateLookupType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLookupType(
            @PathVariable Long id) {
        lookupTypeService.deleteLookupType(id);
        return ResponseEntity.noContent().build();
    }
}
