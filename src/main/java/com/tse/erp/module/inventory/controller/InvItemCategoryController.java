package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.InvItemCategoryRequestDto;
import com.tse.erp.module.inventory.dto.InvItemCategoryResponseDto;
import com.tse.erp.module.inventory.service.InvItemCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-item-categories", "/api/v1/item-categories"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemCategoryController {

    private final InvItemCategoryService itemCategoryService;

    @GetMapping
    public ResponseEntity<List<InvItemCategoryResponseDto>> getAllItemCategories(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(itemCategoryService.getAllItemCategories(bgId, buId, categoryName, categoryCode, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvItemCategoryResponseDto> getItemCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(itemCategoryService.getItemCategoryById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextCategoryCode(@RequestParam Long buId) {
        String code = itemCategoryService.generateNextCategoryCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("categoryCode", code));
    }

    @PostMapping
    public ResponseEntity<InvItemCategoryResponseDto> createItemCategory(
            @RequestBody InvItemCategoryRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemCategoryService.createItemCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvItemCategoryResponseDto> updateItemCategory(
            @PathVariable Long id,
            @RequestBody InvItemCategoryRequestDto request) {
        return ResponseEntity.ok(itemCategoryService.updateItemCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemCategory(@PathVariable Long id) {
        itemCategoryService.deleteItemCategory(id);
        return ResponseEntity.noContent().build();
    }
}

