package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmLookupTypeRequestDto;
import com.tse.erp.module.accounting.dto.AfmLookupTypeResponseDto;
import com.tse.erp.module.accounting.dto.AfmLookupValueDto;
import com.tse.erp.module.accounting.service.AfmLookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-lookup-types", "/api/v1/afmLookupTypes", "/api/v1/afm-lookups"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmLookupTypeController {

    private final AfmLookupTypeService lookupTypeService;

    @GetMapping
    public ResponseEntity<List<AfmLookupTypeResponseDto>> getAllLookupTypes(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(lookupTypeService.getAllLookupTypes(bgId, buId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmLookupTypeResponseDto> getLookupTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(lookupTypeService.getLookupTypeById(id));
    }

    @GetMapping({"/by-code", "/by-type-code"})
    public ResponseEntity<AfmLookupTypeResponseDto> getLookupTypeByCode(
            @RequestParam String typeCode,
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(lookupTypeService.getLookupTypeByCode(typeCode, buId));
    }

    @GetMapping({"/type-code/{typeCode}", "/by-code/{typeCode}", "/by-type-code/{typeCode}"})
    public ResponseEntity<AfmLookupTypeResponseDto> getLookupTypeByTypeCodePath(
            @PathVariable String typeCode,
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(lookupTypeService.getLookupTypeByCode(typeCode, buId));
    }

    @GetMapping("/{id}/values")
    public ResponseEntity<List<AfmLookupValueDto>> getLookupValues(@PathVariable Long id) {
        return ResponseEntity.ok(lookupTypeService.getLookupValues(id));
    }

    /**
     * Retrieves list of values from AfmLookupValue against TYPE_CODE.
     * Supports both query parameter (/values/by-code?typeCode=...) and optional buId and status.
     */
    @GetMapping({"/values/by-code", "/values/by-type-code", "/values-by-code", "/values-by-type-code"})
    public ResponseEntity<List<AfmLookupValueDto>> getLookupValuesByCode(
            @RequestParam String typeCode,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(lookupTypeService.getLookupValuesByCode(typeCode, buId, status));
    }

    /**
     * Retrieves list of values from AfmLookupValue against TYPE_CODE as a path variable.
     * Example: GET /api/v1/afm-lookup-types/type-code/{typeCode}/values
     */
    @GetMapping({
            "/type-code/{typeCode}/values",
            "/by-code/{typeCode}/values",
            "/by-type-code/{typeCode}/values",
            "/values/by-code/{typeCode}",
            "/values/by-type-code/{typeCode}"
    })
    public ResponseEntity<List<AfmLookupValueDto>> getLookupValuesByTypeCodePath(
            @PathVariable String typeCode,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(lookupTypeService.getLookupValuesByCode(typeCode, buId, status));
    }

    @PostMapping
    public ResponseEntity<AfmLookupTypeResponseDto> createLookupType(
            @RequestBody AfmLookupTypeRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupTypeService.createLookupType(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmLookupTypeResponseDto> updateLookupType(
            @PathVariable Long id,
            @RequestBody AfmLookupTypeRequestDto request) {
        return ResponseEntity.ok(lookupTypeService.updateLookupType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLookupType(@PathVariable Long id) {
        lookupTypeService.deleteLookupType(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{typeId}/values")
    public ResponseEntity<AfmLookupValueDto> addLookupValue(
            @PathVariable Long typeId,
            @RequestBody AfmLookupValueDto detailDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupTypeService.addLookupValue(typeId, detailDto));
    }

    @PutMapping("/{typeId}/values/{valueId}")
    public ResponseEntity<AfmLookupValueDto> updateLookupValue(
            @PathVariable Long typeId,
            @PathVariable Long valueId,
            @RequestBody AfmLookupValueDto detailDto) {
        return ResponseEntity.ok(lookupTypeService.updateLookupValue(typeId, valueId, detailDto));
    }

    @DeleteMapping("/{typeId}/values/{valueId}")
    public ResponseEntity<Void> deleteLookupValue(
            @PathVariable Long typeId,
            @PathVariable Long valueId) {
        lookupTypeService.deleteLookupValue(typeId, valueId);
        return ResponseEntity.noContent().build();
    }
}
