package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmPTypePThroughMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmPTypePThroughMapResponseDto;
import com.tse.erp.module.accounting.service.AfmPTypePThroughMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-payment-type-through-maps", "/api/v1/afm-p-type-p-through-maps", "/api/v1/payment-type-mappings"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmPTypePThroughMapController {

    private final AfmPTypePThroughMapService mappingService;

    @GetMapping
    public ResponseEntity<List<AfmPTypePThroughMapResponseDto>> getAllMappings(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(mappingService.getAllMappings(bgId, buId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmPTypePThroughMapResponseDto> getMappingById(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.getMappingById(id));
    }

    @GetMapping("/by-payment-type")
    public ResponseEntity<List<AfmPTypePThroughMapResponseDto>> getMappingsByPaymentType(
            @RequestParam Long buId,
            @RequestParam Long paymentTypeId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(mappingService.getMappingsByPaymentType(buId, paymentTypeId, status));
    }

    @PostMapping
    public ResponseEntity<AfmPTypePThroughMapResponseDto> createMapping(
            @RequestBody AfmPTypePThroughMapRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mappingService.createMapping(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmPTypePThroughMapResponseDto> updateMapping(
            @PathVariable Long id,
            @RequestBody AfmPTypePThroughMapRequestDto request) {
        return ResponseEntity.ok(mappingService.updateMapping(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long id) {
        mappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
}
