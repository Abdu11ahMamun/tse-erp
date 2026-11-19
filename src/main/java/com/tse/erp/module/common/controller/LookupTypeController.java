package com.tse.erp.module.common.controller;

import com.tse.erp.module.common.dto.LookupTypeRequestDto;
import com.tse.erp.module.common.dto.LookupTypeResponseDto;
import com.tse.erp.module.common.dto.LookupValueRowDto;
import com.tse.erp.module.common.service.LookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lookup-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LookupTypeController {

    private final LookupTypeService lookupTypeService;

    @GetMapping
    public ResponseEntity<List<LookupTypeResponseDto>> getAllLookupTypes(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                lookupTypeService.getAllLookupTypes(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LookupTypeResponseDto> getLookupTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupTypeService.getLookupTypeById(id));
    }

    @GetMapping("/{id}/values")
    public ResponseEntity<List<LookupValueRowDto>> getLookupValues(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                lookupTypeService.getLookupValues(id));
    }

    @PostMapping
    public ResponseEntity<LookupTypeResponseDto> createLookupType(
            @RequestBody LookupTypeRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lookupTypeService.createLookupType(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LookupTypeResponseDto> updateLookupType(
            @PathVariable Long id,
            @RequestBody LookupTypeRequestDto request) {
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
