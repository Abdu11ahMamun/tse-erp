package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.dto.CustodianRequestDto;
import com.tse.erp.module.asset.dto.CustodianResponseDto;
import com.tse.erp.module.asset.service.CustodianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/custodians")
@RequiredArgsConstructor
public class CustodianController {

    private final CustodianService custodianService;

    @GetMapping
    public ResponseEntity<List<CustodianResponseDto>> getAllCustodians(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(custodianService.getAllCustodians(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustodianResponseDto> getCustodianById(
            @PathVariable Long id) {
        return ResponseEntity.ok(custodianService.getCustodianById(id));
    }

    @PostMapping
    public ResponseEntity<CustodianResponseDto> createCustodian(
            @RequestBody CustodianRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(custodianService.createCustodian(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustodianResponseDto> updateCustodian(
            @PathVariable Long id,
            @RequestBody CustodianRequestDto request) {
        return ResponseEntity.ok(
                custodianService.updateCustodian(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustodian(
            @PathVariable Long id) {
        custodianService.deleteCustodian(id);
        return ResponseEntity.noContent().build();
    }
}
