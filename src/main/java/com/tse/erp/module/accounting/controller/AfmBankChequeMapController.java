package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmBankChequeMapRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapResponseDto;
import com.tse.erp.module.accounting.dto.AfmBankChequeMapUpdateDto;
import com.tse.erp.module.accounting.service.AfmBankChequeMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-bank-cheque-maps", "/api/v1/bank-cheque-mappings"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmBankChequeMapController {

    private final AfmBankChequeMapService chequeMapService;

    @GetMapping
    public ResponseEntity<List<AfmBankChequeMapResponseDto>> getAllChequeMaps(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) Long bankAccountId,
            @RequestParam(required = false) String chequeStatus,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(chequeMapService.getAllChequeMaps(bgId, buId, bankId, bankAccountId, chequeStatus, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmBankChequeMapResponseDto> getChequeMapById(@PathVariable Long id) {
        return ResponseEntity.ok(chequeMapService.getChequeMapById(id));
    }

    @PostMapping
    public ResponseEntity<List<AfmBankChequeMapResponseDto>> createChequeBatch(
            @RequestBody AfmBankChequeMapRequestDto request) {
        List<AfmBankChequeMapResponseDto> response = chequeMapService.createChequeBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmBankChequeMapResponseDto> updateChequeMap(
            @PathVariable Long id,
            @RequestBody AfmBankChequeMapUpdateDto request) {
        return ResponseEntity.ok(chequeMapService.updateChequeMap(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChequeMap(@PathVariable Long id) {
        chequeMapService.deleteChequeMap(id);
        return ResponseEntity.noContent().build();
    }
}

