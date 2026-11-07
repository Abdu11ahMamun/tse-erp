package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmSupplierInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmSupplierInfoResponseDto;
import com.tse.erp.module.accounting.service.AfmSupplierInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/afm-suppliers")
@RequiredArgsConstructor
public class AfmSupplierInfoController {

    private final AfmSupplierInfoService supplierInfoService;

    @GetMapping
    public ResponseEntity<List<AfmSupplierInfoResponseDto>> getAllSuppliers(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String supplierType,
            @RequestParam(required = false) Integer status
    ) {
        return ResponseEntity.ok(
                supplierInfoService.getAllSuppliers(bgId, buId, query, supplierType, status)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmSupplierInfoResponseDto> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierInfoService.getSupplierById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextSupplierCode(@RequestParam Long buId) {
        String nextCode = supplierInfoService.generateNextSupplierCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("supplierCode", nextCode));
    }

    @PostMapping
    public ResponseEntity<AfmSupplierInfoResponseDto> createSupplier(
            @RequestBody AfmSupplierInfoRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(supplierInfoService.createSupplier(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmSupplierInfoResponseDto> updateSupplier(
            @PathVariable Long id,
            @RequestBody AfmSupplierInfoRequestDto request
    ) {
        return ResponseEntity.ok(supplierInfoService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierInfoService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}

