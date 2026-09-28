package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.service.BusinessUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/business-units")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BusinessUnitController {

    private final BusinessUnitService businessUnitService;

    @GetMapping
    public ResponseEntity<List<BusinessUnit>> getAllBusinessUnits() {
        return ResponseEntity.ok(
                businessUnitService.getAllBusinessUnits());
    }

    @GetMapping("/group/{bgId}")
    public ResponseEntity<List<BusinessUnit>>
    getBusinessUnitsByGroup(@PathVariable Long bgId) {
        return ResponseEntity.ok(
                businessUnitService
                        .getBusinessUnitsByGroupId(bgId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessUnit> getBusinessUnitById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                businessUnitService.getBusinessUnitById(id));
    }

    @PostMapping
    public ResponseEntity<BusinessUnit> createBusinessUnit(
            @RequestBody BusinessUnit businessUnit) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(businessUnitService
                        .createBusinessUnit(businessUnit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessUnit> updateBusinessUnit(
            @PathVariable Long id,
            @RequestBody BusinessUnit businessUnit) {
        return ResponseEntity.ok(
                businessUnitService
                        .updateBusinessUnit(id, businessUnit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessUnit(
            @PathVariable Long id) {
        businessUnitService.deleteBusinessUnit(id);
        return ResponseEntity.noContent().build();
    }
}