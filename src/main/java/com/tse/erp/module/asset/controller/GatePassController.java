package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.dto.GatePassDto;
import com.tse.erp.module.asset.service.GatePassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gate-passes")
@RequiredArgsConstructor
public class GatePassController {

    private final GatePassService service;

    @GetMapping
    public ResponseEntity<List<GatePassDto>> getAll(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(service.getAll(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GatePassDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<GatePassDto> create(@RequestBody GatePassDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatePassDto> update(
            @PathVariable Long id, @RequestBody GatePassDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}