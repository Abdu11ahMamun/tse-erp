package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.InvItemBrandRequestDto;
import com.tse.erp.module.inventory.dto.InvItemBrandResponseDto;
import com.tse.erp.module.inventory.service.InvItemBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-item-brands", "/api/v1/item-brands"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemBrandController {

    private final InvItemBrandService itemBrandService;

    @GetMapping
    public ResponseEntity<List<InvItemBrandResponseDto>> getAllItemBrands(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) String brandCode,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(itemBrandService.getAllItemBrands(bgId, buId, brandName, brandCode, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvItemBrandResponseDto> getItemBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(itemBrandService.getItemBrandById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextBrandCode(@RequestParam Long buId) {
        String code = itemBrandService.generateNextBrandCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("brandCode", code));
    }

    @PostMapping
    public ResponseEntity<InvItemBrandResponseDto> createItemBrand(
            @RequestBody InvItemBrandRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemBrandService.createItemBrand(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvItemBrandResponseDto> updateItemBrand(
            @PathVariable Long id,
            @RequestBody InvItemBrandRequestDto request) {
        return ResponseEntity.ok(itemBrandService.updateItemBrand(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemBrand(@PathVariable Long id) {
        itemBrandService.deleteItemBrand(id);
        return ResponseEntity.noContent().build();
    }
}

