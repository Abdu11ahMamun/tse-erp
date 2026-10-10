package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.Designation;
import com.tse.erp.module.hr.service.DesignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @GetMapping
    public ResponseEntity<List<Designation>> getAllDesignations(
            @RequestParam(required = false) Long bgId) {
        return ResponseEntity.ok(
                designationService.getAllDesignations(bgId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Designation> getDesignationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                designationService.getDesignationById(id));
    }

    @PostMapping
    public ResponseEntity<Designation> createDesignation(
            @RequestBody Designation designation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(designationService.createDesignation(designation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Designation> updateDesignation(
            @PathVariable Long id,
            @RequestBody Designation designation) {
        return ResponseEntity.ok(
                designationService.updateDesignation(id, designation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignation(
            @PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.noContent().build();
    }
}
