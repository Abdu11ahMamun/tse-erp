package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmCostCenterRequestDto;
import com.tse.erp.module.accounting.dto.AfmCostCenterResponseDto;
import com.tse.erp.module.accounting.service.AfmCostCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-cost-centers", "/api/v1/afmCostCenters", "/api/v1/cost-centers"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmCostCenterController {

    private final AfmCostCenterService costCenterService;

    @GetMapping
    public ResponseEntity<List<AfmCostCenterResponseDto>> getAllCostCenters(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(
                costCenterService.getAllCostCenters(bgId, buId, projectId, departmentId, status)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmCostCenterResponseDto> getCostCenterById(@PathVariable Long id) {
        return ResponseEntity.ok(costCenterService.getCostCenterById(id));
    }

    @PostMapping
    public ResponseEntity<AfmCostCenterResponseDto> createCostCenter(
            @RequestBody AfmCostCenterRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(costCenterService.createCostCenter(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmCostCenterResponseDto> updateCostCenter(
            @PathVariable Long id,
            @RequestBody AfmCostCenterRequestDto request) {
        return ResponseEntity.ok(costCenterService.updateCostCenter(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCostCenter(@PathVariable Long id) {
        costCenterService.deleteCostCenter(id);
        return ResponseEntity.noContent().build();
    }
}

