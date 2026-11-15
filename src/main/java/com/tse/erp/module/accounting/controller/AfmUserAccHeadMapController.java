package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmUserAccHeadMapDto;
import com.tse.erp.module.accounting.service.AfmUserAccHeadMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-user-acc-head-maps", "/api/v1/user-acc-head-maps"})
@RequiredArgsConstructor
public class AfmUserAccHeadMapController {

    private final AfmUserAccHeadMapService userAccHeadMapService;

    @GetMapping
    public ResponseEntity<List<AfmUserAccHeadMapDto>> getByUser(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(userAccHeadMapService.getByUserId(userId, buId));
    }

    @PostMapping
    public ResponseEntity<AfmUserAccHeadMapDto> create(@RequestBody AfmUserAccHeadMapDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccHeadMapService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmUserAccHeadMapDto> update(@PathVariable Long id, @RequestBody AfmUserAccHeadMapDto dto) {
        return ResponseEntity.ok(userAccHeadMapService.update(id, dto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<AfmUserAccHeadMapDto>> saveAllForUser(
            @RequestParam Long bgId,
            @RequestParam Long buId,
            @RequestParam Long userId,
            @RequestBody List<AfmUserAccHeadMapDto> items) {
        return ResponseEntity.ok(userAccHeadMapService.saveAllForUser(bgId, buId, userId, items));
    }
}
