package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.WifiInformation;
import com.tse.erp.module.hr.service.WifiInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wifi-informations")
@RequiredArgsConstructor
public class WifiInformationController {

    private final WifiInformationService wifiInformationService;

    @GetMapping
    public ResponseEntity<List<WifiInformation>> getAllWifiInformations(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                wifiInformationService.getAllWifiInformations(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WifiInformation> getWifiInformationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                wifiInformationService.getWifiInformationById(id));
    }

    @PostMapping
    public ResponseEntity<WifiInformation> createWifiInformation(
            @RequestBody WifiInformation wifiInformation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(wifiInformationService
                        .createWifiInformation(wifiInformation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WifiInformation> updateWifiInformation(
            @PathVariable Long id,
            @RequestBody WifiInformation wifiInformation) {
        return ResponseEntity.ok(
                wifiInformationService.updateWifiInformation(
                        id, wifiInformation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWifiInformation(
            @PathVariable Long id) {
        wifiInformationService.deleteWifiInformation(id);
        return ResponseEntity.noContent().build();
    }
}
