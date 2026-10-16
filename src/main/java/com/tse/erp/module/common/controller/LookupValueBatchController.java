package com.tse.erp.module.common.controller;

import com.tse.erp.module.common.dto.LookupValueBatchDto;
import com.tse.erp.module.common.service.LookupValueBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lookup-types/{typeId}/values")
@RequiredArgsConstructor
public class LookupValueBatchController {

    private final LookupValueBatchService lookupValueBatchService;

    @GetMapping
    public ResponseEntity<LookupValueBatchDto> getByType(
            @PathVariable Long typeId) {
        return ResponseEntity.ok(
                lookupValueBatchService.getByType(typeId));
    }

    @PutMapping
    public ResponseEntity<LookupValueBatchDto> saveByType(
            @PathVariable Long typeId,
            @RequestBody LookupValueBatchDto dto) {
        return ResponseEntity.ok(
                lookupValueBatchService.saveByType(typeId, dto));
    }
}
