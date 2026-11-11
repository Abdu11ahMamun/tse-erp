package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.InvItemAttributeDtlsDto;
import com.tse.erp.module.inventory.dto.InvItemRequestDto;
import com.tse.erp.module.inventory.dto.InvItemResponseDto;
import com.tse.erp.module.inventory.service.InvItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-items", "/api/v1/items"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemController {

    private final InvItemService itemService;

    @GetMapping
    public ResponseEntity<List<InvItemResponseDto>> getAllItems(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long uomId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(itemService.getAllItems(bgId, buId, categoryId, brandId, uomId, storeId, itemName, itemCode, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvItemResponseDto> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextItemCode(@RequestParam Long buId) {
        String code = itemService.generateNextItemCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("itemCode", code));
    }

    @PostMapping
    public ResponseEntity<InvItemResponseDto> createItem(
            @RequestBody InvItemRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemService.createItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvItemResponseDto> updateItem(
            @PathVariable Long id,
            @RequestBody InvItemRequestDto request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<InvItemAttributeDtlsDto>> getItemDetails(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemDetails(id));
    }

    @PostMapping("/{itemId}/details")
    public ResponseEntity<InvItemAttributeDtlsDto> addItemDetail(
            @PathVariable Long itemId,
            @RequestBody InvItemAttributeDtlsDto detailDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemService.addItemDetail(itemId, detailDto));
    }

    @PutMapping("/{itemId}/details/{detailId}")
    public ResponseEntity<InvItemAttributeDtlsDto> updateItemDetail(
            @PathVariable Long itemId,
            @PathVariable Long detailId,
            @RequestBody InvItemAttributeDtlsDto detailDto) {
        return ResponseEntity.ok(itemService.updateItemDetail(itemId, detailId, detailDto));
    }

    @DeleteMapping("/{itemId}/details/{detailId}")
    public ResponseEntity<Void> deleteItemDetail(
            @PathVariable Long itemId,
            @PathVariable Long detailId) {
        itemService.deleteItemDetail(itemId, detailId);
        return ResponseEntity.noContent().build();
    }
}

