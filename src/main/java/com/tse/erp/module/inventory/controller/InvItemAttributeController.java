package com.tse.erp.module.inventory.controller;

import com.tse.erp.module.inventory.dto.InvItemAttributeRequestDto;
import com.tse.erp.module.inventory.dto.InvItemAttributeResponseDto;
import com.tse.erp.module.inventory.service.InvItemAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/inv-item-attributes", "/api/v1/item-attributes"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvItemAttributeController {

    private final InvItemAttributeService itemAttributeService;

    @GetMapping
    public ResponseEntity<List<InvItemAttributeResponseDto>> getAllItemAttributes(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String attributeName,
            @RequestParam(required = false) String attributeCode,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(itemAttributeService.getAllItemAttributes(bgId, buId, attributeName, attributeCode, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvItemAttributeResponseDto> getItemAttributeById(@PathVariable Long id) {
        return ResponseEntity.ok(itemAttributeService.getItemAttributeById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextAttributeCode(@RequestParam Long buId) {
        String code = itemAttributeService.generateNextAttributeCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("attributeCode", code));
    }

    @PostMapping
    public ResponseEntity<InvItemAttributeResponseDto> createItemAttribute(
            @RequestBody InvItemAttributeRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(itemAttributeService.createItemAttribute(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvItemAttributeResponseDto> updateItemAttribute(
            @PathVariable Long id,
            @RequestBody InvItemAttributeRequestDto request) {
        return ResponseEntity.ok(itemAttributeService.updateItemAttribute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemAttribute(@PathVariable Long id) {
        itemAttributeService.deleteItemAttribute(id);
        return ResponseEntity.noContent().build();
    }
}

