package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.dto.EmpAttConfigDto;
import com.tse.erp.module.hr.service.EmpAttConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-attendance-configs")
@RequiredArgsConstructor
public class EmpAttConfigController {

    private final EmpAttConfigService service;

    @GetMapping
    public ResponseEntity<List<EmpAttConfigDto>> getAll(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(service.getAll(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpAttConfigDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<EmpAttConfigDto> create(
            @RequestBody EmpAttConfigDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpAttConfigDto> update(
            @PathVariable Long id,
            @RequestBody EmpAttConfigDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}