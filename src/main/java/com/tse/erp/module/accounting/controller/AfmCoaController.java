package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.entity.AfmCoa;
import com.tse.erp.module.accounting.service.AfmCoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // React theke call asbe
public class AfmCoaController {

    private final AfmCoaService afmCoaService;

    // =========================================
    // GET ALL
    // =========================================
    @GetMapping
    public ResponseEntity<List<AfmCoa>> getAllCoas() {
        List<AfmCoa> coas = afmCoaService.getAllCoas();
        return ResponseEntity.ok(coas);
    }

    // =========================================
    // GET BY ID
    // =========================================
    @GetMapping("/{id}")
    public ResponseEntity<AfmCoa> getCoaById(@PathVariable Long id) {
        AfmCoa coa = afmCoaService.getCoaById(id);
        return ResponseEntity.ok(coa);
    }

    // =========================================
    // CREATE
    // =========================================
    @PostMapping
    public ResponseEntity<AfmCoa> createCoa(@RequestBody AfmCoa afmCoa) {
        AfmCoa created = afmCoaService.createCoa(afmCoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================================
    // UPDATE
    // =========================================
    @PutMapping("/{id}")
    public ResponseEntity<AfmCoa> updateCoa(
            @PathVariable Long id,
            @RequestBody AfmCoa afmCoa) {
        AfmCoa updated = afmCoaService.updateCoa(id, afmCoa);
        return ResponseEntity.ok(updated);
    }

    // =========================================
    // DELETE
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoa(@PathVariable Long id) {
        afmCoaService.deleteCoa(id);
        return ResponseEntity.noContent().build();
    }
}