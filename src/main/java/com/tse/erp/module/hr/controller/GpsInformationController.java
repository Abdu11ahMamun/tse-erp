package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.GpsInformation;
import com.tse.erp.module.hr.service.GpsInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gps-informations")
@RequiredArgsConstructor
public class GpsInformationController {

    private final GpsInformationService gpsInformationService;

    @GetMapping
    public ResponseEntity<List<GpsInformation>> getAllGpsInformations(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                gpsInformationService.getAllGpsInformations(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GpsInformation> getGpsInformationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                gpsInformationService.getGpsInformationById(id));
    }

    @PostMapping
    public ResponseEntity<GpsInformation> createGpsInformation(
            @RequestBody GpsInformation gpsInformation) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gpsInformationService
                        .createGpsInformation(gpsInformation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GpsInformation> updateGpsInformation(
            @PathVariable Long id,
            @RequestBody GpsInformation gpsInformation) {
        return ResponseEntity.ok(
                gpsInformationService.updateGpsInformation(
                        id, gpsInformation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGpsInformation(
            @PathVariable Long id) {
        gpsInformationService.deleteGpsInformation(id);
        return ResponseEntity.noContent().build();
    }
}
