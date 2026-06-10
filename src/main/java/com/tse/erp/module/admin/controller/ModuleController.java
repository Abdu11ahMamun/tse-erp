package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.Module;
import com.tse.erp.module.admin.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<Module>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Module> getModuleById(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }

    @PostMapping
    public ResponseEntity<Module> createModule(@RequestBody Module module) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(moduleService.createModule(module));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Module> updateModule(
            @PathVariable Long id,
            @RequestBody Module module) {
        return ResponseEntity.ok(moduleService.updateModule(id, module));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}