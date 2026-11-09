package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.InvItemStoreRequestDto;
import com.tse.erp.module.inventory.dto.InvItemStoreResponseDto;
import com.tse.erp.module.inventory.service.InvItemStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-item-stores", "/api/v1/item-stores"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemStoreController {

    private final InvItemStoreService itemStoreService;

    @GetMapping
    public ResponseEntity<List<InvItemStoreResponseDto>> getAllItemStores(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeCode,
            @RequestParam(required = false) Integer isDefault,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(itemStoreService.getAllItemStores(bgId, buId, storeName, storeCode, isDefault, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvItemStoreResponseDto> getItemStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(itemStoreService.getItemStoreById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextStoreCode(@RequestParam Long buId) {
        String code = itemStoreService.generateNextStoreCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("storeCode", code));
    }

    @PostMapping
    public ResponseEntity<InvItemStoreResponseDto> createItemStore(
            @RequestBody InvItemStoreRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemStoreService.createItemStore(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvItemStoreResponseDto> updateItemStore(
            @PathVariable Long id,
            @RequestBody InvItemStoreRequestDto request) {
        return ResponseEntity.ok(itemStoreService.updateItemStore(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemStore(@PathVariable Long id) {
        itemStoreService.deleteItemStore(id);
        return ResponseEntity.noContent().build();
    }
}

