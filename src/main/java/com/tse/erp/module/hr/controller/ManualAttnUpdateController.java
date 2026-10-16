package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.ManualAttnUpdate;
import com.tse.erp.module.hr.service.ManualAttnUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manual-attendance-updates")
@RequiredArgsConstructor
public class ManualAttnUpdateController {

    private final ManualAttnUpdateService service;

    @GetMapping
    public ResponseEntity<List<ManualAttnUpdate>> getAll(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attenDate) {
        return ResponseEntity.ok(service.getAll(buId, attenDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManualAttnUpdate> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ManualAttnUpdate> create(
            @RequestBody ManualAttnUpdate r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManualAttnUpdate> update(
            @PathVariable Long id,
            @RequestBody ManualAttnUpdate r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}