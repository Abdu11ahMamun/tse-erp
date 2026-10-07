package com.tse.erp.module.common.controller;

import com.tse.erp.module.common.entity.LookupType;
import com.tse.erp.module.common.service.LookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lookup-types")
@RequiredArgsConstructor
public class LookupTypeController {

    private final LookupTypeService lookupTypeService;

    @GetMapping
    public ResponseEntity<List<LookupType>> getAllLookupTypes(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                lookupTypeService.getAllLookupTypes(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LookupType> getLookupTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupTypeService.getLookupTypeById(id));
    }

    @PostMapping
    public ResponseEntity<LookupType> createLookupType(
            @RequestBody LookupType lookupType) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupTypeService.createLookupType(lookupType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LookupType> updateLookupType(
            @PathVariable Long id,
            @RequestBody LookupType lookupType) {
        return ResponseEntity.ok(
                lookupTypeService.updateLookupType(id, lookupType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLookupType(
            @PathVariable Long id) {
        lookupTypeService.deleteLookupType(id);
        return ResponseEntity.noContent().build();
    }
}