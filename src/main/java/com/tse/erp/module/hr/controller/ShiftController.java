package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.Shift;
import com.tse.erp.module.hr.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<List<Shift>> getAllShifts(
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(shiftService.getAllShifts(buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shift> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @PostMapping
    public ResponseEntity<Shift> createShift(@RequestBody Shift shift) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(shiftService.createShift(shift));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shift> updateShift(
            @PathVariable Long id, @RequestBody Shift shift) {
        return ResponseEntity.ok(shiftService.updateShift(id, shift));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }
}
