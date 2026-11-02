package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmBankDtlsDto;
import com.tse.erp.module.accounting.dto.AfmBankRequestDto;
import com.tse.erp.module.accounting.dto.AfmBankResponseDto;
import com.tse.erp.module.accounting.service.AfmBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-banks", "/api/v1/afmBanks", "/api/v1/banks"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmBankController {

    private final AfmBankService bankService;

    @GetMapping
    public ResponseEntity<List<AfmBankResponseDto>> getAllBanks(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(bankService.getAllBanks(bgId, buId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmBankResponseDto> getBankById(@PathVariable Long id) {
        return ResponseEntity.ok(bankService.getBankById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<AfmBankDtlsDto>> getBankDetails(@PathVariable Long id) {
        return ResponseEntity.ok(bankService.getBankDetails(id));
    }

    @PostMapping
    public ResponseEntity<AfmBankResponseDto> createBank(
            @RequestBody AfmBankRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankService.createBank(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmBankResponseDto> updateBank(
            @PathVariable Long id,
            @RequestBody AfmBankRequestDto request) {
        return ResponseEntity.ok(bankService.updateBank(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bankId}/details")
    public ResponseEntity<AfmBankDtlsDto> addBankDetail(
            @PathVariable Long bankId,
            @RequestBody AfmBankDtlsDto detailDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankService.addBankDetail(bankId, detailDto));
    }

    @PutMapping("/{bankId}/details/{detailId}")
    public ResponseEntity<AfmBankDtlsDto> updateBankDetail(
            @PathVariable Long bankId,
            @PathVariable Long detailId,
            @RequestBody AfmBankDtlsDto detailDto) {
        return ResponseEntity.ok(bankService.updateBankDetail(bankId, detailId, detailDto));
    }

    @DeleteMapping("/{bankId}/details/{detailId}")
    public ResponseEntity<Void> deleteBankDetail(
            @PathVariable Long bankId,
            @PathVariable Long detailId) {
        bankService.deleteBankDetail(bankId, detailId);
        return ResponseEntity.noContent().build();
    }
}
