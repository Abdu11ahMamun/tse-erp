package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.BusinessGroup;
import com.tse.erp.module.admin.service.BusinessGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/business-groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BusinessGroupController {

    private final BusinessGroupService businessGroupService;

    @GetMapping
    public ResponseEntity<List<BusinessGroup>> getAllBusinessGroups() {
        return ResponseEntity.ok(
                businessGroupService.getAllBusinessGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessGroup> getBusinessGroupById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                businessGroupService.getBusinessGroupById(id));
    }

    @PostMapping
    public ResponseEntity<BusinessGroup> createBusinessGroup(
            @RequestBody BusinessGroup businessGroup) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(businessGroupService
                        .createBusinessGroup(businessGroup));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessGroup> updateBusinessGroup(
            @PathVariable Long id,
            @RequestBody BusinessGroup businessGroup) {
        return ResponseEntity.ok(
                businessGroupService
                        .updateBusinessGroup(id, businessGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusinessGroup(
            @PathVariable Long id) {
        businessGroupService.deleteBusinessGroup(id);
        return ResponseEntity.noContent().build();
    }
}