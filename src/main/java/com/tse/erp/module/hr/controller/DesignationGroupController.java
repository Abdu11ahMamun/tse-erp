package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.DesignationGroup;
import com.tse.erp.module.hr.service.DesignationGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/designation-groups")
@RequiredArgsConstructor
public class DesignationGroupController {

    private final DesignationGroupService designationGroupService;

    @GetMapping
    public ResponseEntity<List<DesignationGroup>> getAllDesignationGroups(
            @RequestParam(required = false) Long desigId) {
        return ResponseEntity.ok(
                designationGroupService.getAllDesignationGroups(desigId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationGroup> getDesignationGroupById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                designationGroupService.getDesignationGroupById(id));
    }

    @PostMapping
    public ResponseEntity<DesignationGroup> createDesignationGroup(
            @RequestBody DesignationGroup designationGroup) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(designationGroupService
                        .createDesignationGroup(designationGroup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationGroup> updateDesignationGroup(
            @PathVariable Long id,
            @RequestBody DesignationGroup designationGroup) {
        return ResponseEntity.ok(
                designationGroupService
                        .updateDesignationGroup(id, designationGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignationGroup(
            @PathVariable Long id) {
        designationGroupService.deleteDesignationGroup(id);
        return ResponseEntity.noContent().build();
    }
}
