package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.DeptDesigMap;
import com.tse.erp.module.hr.service.DeptDesigMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dept-desig-maps")
@RequiredArgsConstructor
public class DeptDesigMapController {

    private final DeptDesigMapService deptDesigMapService;

    @GetMapping
    public ResponseEntity<List<DeptDesigMap>> getAllMappings(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                deptDesigMapService.getAllMappings(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeptDesigMap> getMappingById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                deptDesigMapService.getMappingById(id));
    }

    @PostMapping
    public ResponseEntity<DeptDesigMap> createMapping(
            @RequestBody DeptDesigMap mapping) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(deptDesigMapService.createMapping(mapping));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeptDesigMap> updateMapping(
            @PathVariable Long id,
            @RequestBody DeptDesigMap mapping) {
        return ResponseEntity.ok(
                deptDesigMapService.updateMapping(id, mapping));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(
            @PathVariable Long id) {
        deptDesigMapService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
}
