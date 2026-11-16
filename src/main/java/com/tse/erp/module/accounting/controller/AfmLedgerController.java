package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmLedgerAttachDto;
import com.tse.erp.module.accounting.dto.AfmLedgerDetailDto;
import com.tse.erp.module.accounting.dto.AfmLedgerRequestDto;
import com.tse.erp.module.accounting.dto.AfmLedgerResponseDto;
import com.tse.erp.module.accounting.service.AfmLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-ledgers", "/api/v1/afmLedgers", "/api/v1/ledgers"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmLedgerController {

    private final AfmLedgerService ledgerService;

    @GetMapping
    public ResponseEntity<List<AfmLedgerResponseDto>> getAllLedgers(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ledgerService.getAllLedgers(bgId, buId, voucherType, status, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmLedgerResponseDto> getLedgerById(@PathVariable Long id) {
        return ResponseEntity.ok(ledgerService.getLedgerById(id));
    }

    @GetMapping("/by-no")
    public ResponseEntity<AfmLedgerResponseDto> getLedgerByNo(
            @RequestParam Long buId,
            @RequestParam String voucherNo) {
        return ResponseEntity.ok(ledgerService.getLedgerByNo(buId, voucherNo));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<AfmLedgerDetailDto>> getLedgerDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ledgerService.getLedgerDetails(id));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<AfmLedgerAttachDto>> getLedgerAttachments(@PathVariable Long id) {
        return ResponseEntity.ok(ledgerService.getLedgerAttachments(id));
    }

    @PostMapping
    public ResponseEntity<AfmLedgerResponseDto> createLedger(
            @RequestBody AfmLedgerRequestDto request) {
        return new ResponseEntity<>(ledgerService.createLedger(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmLedgerResponseDto> updateLedger(
            @PathVariable Long id,
            @RequestBody AfmLedgerRequestDto request) {
        return ResponseEntity.ok(ledgerService.updateLedger(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLedger(@PathVariable Long id) {
        ledgerService.deleteLedger(id);
        return ResponseEntity.noContent().build();
    }
}
