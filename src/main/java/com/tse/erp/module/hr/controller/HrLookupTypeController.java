package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.HrLookupType;
import com.tse.erp.module.hr.service.HrLookupTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr-lookup-types")
@RequiredArgsConstructor
public class HrLookupTypeController {

    private final HrLookupTypeService hrLookupTypeService;

    @GetMapping
    public ResponseEntity<List<HrLookupType>> getAllHrLookupTypes() {
        return ResponseEntity.ok(
                hrLookupTypeService.getAllHrLookupTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HrLookupType> getHrLookupTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                hrLookupTypeService.getHrLookupTypeById(id));
    }

    @PostMapping
    public ResponseEntity<HrLookupType> createHrLookupType(
            @RequestBody HrLookupType hrLookupType) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(hrLookupTypeService.createHrLookupType(hrLookupType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HrLookupType> updateHrLookupType(
            @PathVariable Long id,
            @RequestBody HrLookupType hrLookupType) {
        return ResponseEntity.ok(
                hrLookupTypeService
                        .updateHrLookupType(id, hrLookupType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHrLookupType(
            @PathVariable Long id) {
        hrLookupTypeService.deleteHrLookupType(id);
        return ResponseEntity.noContent().build();
    }
}
