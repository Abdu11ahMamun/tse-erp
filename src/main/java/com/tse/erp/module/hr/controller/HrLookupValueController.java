package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.HrLookupValue;
import com.tse.erp.module.hr.service.HrLookupValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr-lookup-values")
@RequiredArgsConstructor
public class HrLookupValueController {

    private final HrLookupValueService hrLookupValueService;

    @GetMapping
    public ResponseEntity<List<HrLookupValue>> getAllHrLookupValues(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long typeId) {
        return ResponseEntity.ok(
                hrLookupValueService.getAllHrLookupValues(bgId, typeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HrLookupValue> getHrLookupValueById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                hrLookupValueService.getHrLookupValueById(id));
    }

    @PostMapping
    public ResponseEntity<HrLookupValue> createHrLookupValue(
            @RequestBody HrLookupValue hrLookupValue) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(hrLookupValueService
                        .createHrLookupValue(hrLookupValue));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HrLookupValue> updateHrLookupValue(
            @PathVariable Long id,
            @RequestBody HrLookupValue hrLookupValue) {
        return ResponseEntity.ok(
                hrLookupValueService
                        .updateHrLookupValue(id, hrLookupValue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHrLookupValue(
            @PathVariable Long id) {
        hrLookupValueService.deleteHrLookupValue(id);
        return ResponseEntity.noContent().build();
    }
}
