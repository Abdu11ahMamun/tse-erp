package com.tse.erp.module.common.controller;

import com.tse.erp.module.common.entity.LookupValue;
import com.tse.erp.module.common.service.LookupValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lookup-values")
@RequiredArgsConstructor
public class LookupValueController {

    private final LookupValueService lookupValueService;

    @GetMapping
    public ResponseEntity<List<LookupValue>> getAllLookupValues(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long typeId) {
        return ResponseEntity.ok(
                lookupValueService.getAllLookupValues(buId, typeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LookupValue> getLookupValueById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupValueService.getLookupValueById(id));
    }

    @PostMapping
    public ResponseEntity<LookupValue> createLookupValue(
            @RequestBody LookupValue lookupValue) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupValueService.createLookupValue(lookupValue));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LookupValue> updateLookupValue(
            @PathVariable Long id,
            @RequestBody LookupValue lookupValue) {
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