package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.UserTypeGroup;
import com.tse.erp.module.admin.service.UserTypeGroupMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-type-groups")
@RequiredArgsConstructor
public class UserTypeGroupMapController {

    private final UserTypeGroupMapService userTypeGroupMapService;

    @GetMapping
    public ResponseEntity<List<UserTypeGroup>> getAll(
            @RequestParam(required = false) Long userTypeId,
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                userTypeGroupMapService.getAll(userTypeId, buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserTypeGroup> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                userTypeGroupMapService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserTypeGroup> create(
            @RequestBody UserTypeGroup row) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userTypeGroupMapService.create(row));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserTypeGroup> update(
            @PathVariable Long id,
            @RequestBody UserTypeGroup row) {
        return ResponseEntity.ok(
                userTypeGroupMapService.update(id, row));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        userTypeGroupMapService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
