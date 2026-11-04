package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmUserLocationRequestDto;
import com.tse.erp.module.accounting.dto.AfmUserLocationResponseDto;
import com.tse.erp.module.accounting.service.AfmUserLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-user-locations", "/api/v1/user-locations", "/api/v1/locations"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmUserLocationController {

    private final AfmUserLocationService userLocationService;

    @GetMapping
    public ResponseEntity<List<AfmUserLocationResponseDto>> getAllUserLocations(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(userLocationService.getAllUserLocations(bgId, buId, location, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmUserLocationResponseDto> getUserLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(userLocationService.getUserLocationById(id));
    }

    @PostMapping
    public ResponseEntity<AfmUserLocationResponseDto> createUserLocation(
            @RequestBody AfmUserLocationRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userLocationService.createUserLocation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmUserLocationResponseDto> updateUserLocation(
            @PathVariable Long id,
            @RequestBody AfmUserLocationRequestDto request) {
        return ResponseEntity.ok(userLocationService.updateUserLocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserLocation(@PathVariable Long id) {
        userLocationService.deleteUserLocation(id);
        return ResponseEntity.noContent().build();
    }
}

