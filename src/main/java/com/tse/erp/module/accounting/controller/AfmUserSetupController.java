package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmUserSetupRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserSetupResponseDto;
import com.tse.erp.module.accounting.dto.UserOptionDto;
import com.tse.erp.module.accounting.service.AfmUserSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-user-setups", "/api/v1/user-setups"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmUserSetupController {

    private final AfmUserSetupService userSetupService;

    @GetMapping
    public ResponseEntity<List<AfmUserSetupResponseDto>> getAllUserSetups(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(userSetupService.getAllUserSetups(bgId, buId, userId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmUserSetupResponseDto> getUserSetupById(@PathVariable Long id) {
        return ResponseEntity.ok(userSetupService.getUserSetupById(id));
    }

    @PostMapping
    public ResponseEntity<AfmUserSetupResponseDto> createUserSetup(
            @RequestBody AfmUserSetupRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userSetupService.createUserSetup(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmUserSetupResponseDto> updateUserSetup(
            @PathVariable Long id,
            @RequestBody AfmUserSetupRequestDto request) {
        return ResponseEntity.ok(userSetupService.updateUserSetup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserSetup(@PathVariable Long id) {
        userSetupService.deleteUserSetup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-users")
    public ResponseEntity<List<UserOptionDto>> getAvailableUsers(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(userSetupService.getAvailableUsers(bgId, buId));
    }
}

