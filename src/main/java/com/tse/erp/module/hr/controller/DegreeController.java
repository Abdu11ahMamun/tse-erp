package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.Degree;
import com.tse.erp.module.hr.service.DegreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/degrees")
@RequiredArgsConstructor
public class DegreeController {

    private final DegreeService degreeService;

    @GetMapping
    public ResponseEntity<List<Degree>> getAllDegrees(
            @RequestParam(required = false) Long levelOfEduId) {
        return ResponseEntity.ok(
                degreeService.getAllDegrees(levelOfEduId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Degree> getDegreeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                degreeService.getDegreeById(id));
    }

    @PostMapping
    public ResponseEntity<Degree> createDegree(
            @RequestBody Degree degree) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(degreeService.createDegree(degree));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Degree> updateDegree(
            @PathVariable Long id,
            @RequestBody Degree degree) {
        return ResponseEntity.ok(
                degreeService.updateDegree(id, degree));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDegree(
            @PathVariable Long id) {
        degreeService.deleteDegree(id);
        return ResponseEntity.noContent().build();
    }
}
