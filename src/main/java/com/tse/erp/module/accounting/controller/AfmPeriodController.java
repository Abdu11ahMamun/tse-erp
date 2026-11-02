package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmPeriodRequestDto;
import com.tse.erp.module.accounting.dto.AfmPeriodResponseDto;
import com.tse.erp.module.accounting.service.AfmPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-periods", "/api/v1/accounting-periods", "/api/v1/periods"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmPeriodController {

    private final AfmPeriodService periodService;

    @GetMapping
    public ResponseEntity<List<AfmPeriodResponseDto>> getAllPeriods(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String periodYear,
            @RequestParam(required = false) String periodStatus,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(periodService.getAllPeriods(bgId, buId, periodYear, periodStatus, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmPeriodResponseDto> getPeriodById(@PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriodById(id));
    }

    @PostMapping
    public ResponseEntity<AfmPeriodResponseDto> createPeriod(
            @RequestBody AfmPeriodRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(periodService.createPeriod(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmPeriodResponseDto> updatePeriod(
            @PathVariable Long id,
            @RequestBody AfmPeriodRequestDto request) {
        return ResponseEntity.ok(periodService.updatePeriod(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        periodService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/open-periods")
    public ResponseEntity<List<String>> getOpenPeriodNames(
            @RequestParam Long buId,
            @RequestParam String year) {
        return ResponseEntity.ok(periodService.getOpenPeriodNames(buId, year));
    }

    @GetMapping("/years")
    public ResponseEntity<List<String>> getAvailableYears(
            @RequestParam Long buId) {
        return ResponseEntity.ok(periodService.getAvailableYears(buId));
    }
}

