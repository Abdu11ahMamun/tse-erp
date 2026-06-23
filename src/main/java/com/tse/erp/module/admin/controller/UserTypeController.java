package com.tse.erp.module.admin.controller;

import com.tse.erp.module.admin.entity.UserType;
import com.tse.erp.module.admin.service.UserTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserTypeController {

    private final UserTypeService userTypeService;

    @GetMapping
    public ResponseEntity<List<UserType>> getAllUserTypes() {
        return ResponseEntity.ok(userTypeService.getAllUserTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserType> getUserTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(userTypeService.getUserTypeById(id));
    }

    @PostMapping
    public ResponseEntity<UserType> createUserType(
            @RequestBody UserType userType) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userTypeService.createUserType(userType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserType> updateUserType(
            @PathVariable Long id,
            @RequestBody UserType userType) {
        return ResponseEntity.ok(
                userTypeService.updateUserType(id, userType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserType(@PathVariable Long id) {
        userTypeService.deleteUserType(id);
        return ResponseEntity.noContent().build();
    }
}