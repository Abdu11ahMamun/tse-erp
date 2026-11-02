package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmVTypePTypeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmVTypePTypeMapResponseDto;
import com.tse.erp.module.accounting.service.AfmVTypePTypeMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-voucher-type-payment-type-maps", "/api/v1/afm-v-type-p-type-maps", "/api/v1/voucher-type-mappings"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmVTypePTypeMapController {

    private final AfmVTypePTypeMapService mappingService;

    @GetMapping
    public ResponseEntity<List<AfmVTypePTypeMapResponseDto>> getAllMappings(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(mappingService.getAllMappings(bgId, buId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmVTypePTypeMapResponseDto> getMappingById(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.getMappingById(id));
    }

    @GetMapping("/by-voucher-type")
    public ResponseEntity<List<AfmVTypePTypeMapResponseDto>> getMappingsByVoucherType(
            @RequestParam Long buId,
            @RequestParam Long voucherTypeId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(mappingService.getMappingsByVoucherType(buId, voucherTypeId, status));
    }

    @PostMapping
    public ResponseEntity<AfmVTypePTypeMapResponseDto> createMapping(
            @RequestBody AfmVTypePTypeMapRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mappingService.createMapping(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmVTypePTypeMapResponseDto> updateMapping(
            @PathVariable Long id,
            @RequestBody AfmVTypePTypeMapRequestDto request) {
        return ResponseEntity.ok(mappingService.updateMapping(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long id) {
        mappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
}