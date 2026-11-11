package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.InvItemWarehouseRequestDto;
import com.tse.erp.module.inventory.dto.InvItemWarehouseResponseDto;
import com.tse.erp.module.inventory.service.InvItemWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-item-warehouses", "/api/v1/item-warehouses"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemWarehouseController {

    private final InvItemWarehouseService itemWarehouseService;

    @GetMapping
    public ResponseEntity<List<InvItemWarehouseResponseDto>> getAllItemWarehouses(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) Integer isDefault,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(itemWarehouseService.getAllItemWarehouses(bgId, buId, warehouseName, warehouseCode, storeId, isDefault, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvItemWarehouseResponseDto> getItemWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(itemWarehouseService.getItemWarehouseById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextWarehouseCode(@RequestParam Long buId) {
        String code = itemWarehouseService.generateNextWarehouseCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("warehouseCode", code));
    }

    @PostMapping
    public ResponseEntity<InvItemWarehouseResponseDto> createItemWarehouse(
            @RequestBody InvItemWarehouseRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemWarehouseService.createItemWarehouse(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvItemWarehouseResponseDto> updateItemWarehouse(
            @PathVariable Long id,
            @RequestBody InvItemWarehouseRequestDto request) {
        return ResponseEntity.ok(itemWarehouseService.updateItemWarehouse(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemWarehouse(@PathVariable Long id) {
        itemWarehouseService.deleteItemWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}

