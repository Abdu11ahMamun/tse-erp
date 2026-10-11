package com.tse.erp.module.asset.controller;

import com.tse.erp.module.asset.dto.AssetReceiveDto;
import com.tse.erp.module.asset.service.AssetReceiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-receives")
@RequiredArgsConstructor
public class AssetReceiveController {

    private final AssetReceiveService assetReceiveService;

    @GetMapping
    public ResponseEntity<List<AssetReceiveDto>> getAllAssetReceives(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(
                assetReceiveService.getAllAssetReceives(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetReceiveDto> getAssetReceiveById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                assetReceiveService.getAssetReceiveById(id));
    }

    @PostMapping
    public ResponseEntity<AssetReceiveDto> createAssetReceive(
            @RequestBody AssetReceiveDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assetReceiveService.createAssetReceive(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetReceiveDto> updateAssetReceive(
            @PathVariable Long id,
            @RequestBody AssetReceiveDto dto) {
        return ResponseEntity.ok(
                assetReceiveService.updateAssetReceive(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AssetReceiveDto> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return ResponseEntity.ok(
                assetReceiveService.changeStatus(id, status));
    }
}
