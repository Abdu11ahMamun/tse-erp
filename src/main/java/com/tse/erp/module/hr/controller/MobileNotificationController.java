package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.dto.MobileNotificationCreateDto;
import com.tse.erp.module.hr.entity.MobileNotification;
import com.tse.erp.module.hr.service.MobileNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mobile-notifications")
@RequiredArgsConstructor
public class MobileNotificationController {

    private final MobileNotificationService mobileNotificationService;

    @GetMapping
    public ResponseEntity<List<MobileNotification>>
    getAllMobileNotifications(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Long emplId) {
        return ResponseEntity.ok(
                mobileNotificationService
                        .getAllMobileNotifications(buId, emplId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MobileNotification> getMobileNotificationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                mobileNotificationService.getMobileNotificationById(id));
    }

    @PostMapping
    public ResponseEntity<List<MobileNotification>>
    createMobileNotifications(
            @RequestBody MobileNotificationCreateDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mobileNotificationService
                        .createMobileNotifications(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MobileNotification> updateMobileNotification(
            @PathVariable Long id,
            @RequestBody MobileNotification row) {
        return ResponseEntity.ok(
                mobileNotificationService
                        .updateMobileNotification(id, row));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMobileNotification(
            @PathVariable Long id) {
        mobileNotificationService.deleteMobileNotification(id);
        return ResponseEntity.noContent().build();
    }
}
