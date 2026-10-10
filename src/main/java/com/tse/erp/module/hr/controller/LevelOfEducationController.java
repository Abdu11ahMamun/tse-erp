package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.LevelOfEducation;
import com.tse.erp.module.hr.service.LevelOfEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/level-of-educations")
@RequiredArgsConstructor
public class LevelOfEducationController {

    private final LevelOfEducationService levelOfEducationService;

    @GetMapping
    public ResponseEntity<List<LevelOfEducation>>
            getAllLevelOfEducations() {
        return ResponseEntity.ok(
                levelOfEducationService.getAllLevelOfEducations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LevelOfEducation> getLevelOfEducationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                levelOfEducationService.getLevelOfEducationById(id));
    }

    @PostMapping
    public ResponseEntity<LevelOfEducation> createLevelOfEducation(
            @RequestBody LevelOfEducation levelOfEducation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(levelOfEducationService
                        .createLevelOfEducation(levelOfEducation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LevelOfEducation> updateLevelOfEducation(
            @PathVariable Long id,
            @RequestBody LevelOfEducation levelOfEducation) {
        return ResponseEntity.ok(
                levelOfEducationService
                        .updateLevelOfEducation(id, levelOfEducation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLevelOfEducation(
            @PathVariable Long id) {
        levelOfEducationService.deleteLevelOfEducation(id);
        return ResponseEntity.noContent().build();
    }
}
