package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.entity.DepriCategory;
import com.tse.erp.module.asset.service.DepriCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/depri-categories")
@RequiredArgsConstructor
public class DepriCategoryController {

    private final DepriCategoryService depriCategoryService;

    @GetMapping
    public ResponseEntity<List<DepriCategory>> getAllDepriCategories(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                depriCategoryService.getAllDepriCategories(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepriCategory> getDepriCategoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                depriCategoryService.getDepriCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<DepriCategory> createDepriCategory(
            @RequestBody DepriCategory depriCategory) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(depriCategoryService
                        .createDepriCategory(depriCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepriCategory> updateDepriCategory(
            @PathVariable Long id,
            @RequestBody DepriCategory depriCategory) {
        return ResponseEntity.ok(
                depriCategoryService
                        .updateDepriCategory(id, depriCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepriCategory(
            @PathVariable Long id) {
        depriCategoryService.deleteDepriCategory(id);
        return ResponseEntity.noContent().build();
    }
}
