package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.dto.OfficeCalendarDto;
import com.tse.erp.module.hr.service.OfficeCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/office-calendars")
@RequiredArgsConstructor
public class OfficeCalendarController {

    private final OfficeCalendarService officeCalendarService;

    @GetMapping
    public ResponseEntity<List<OfficeCalendarDto>> getAllOfficeCalendars(
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(
                officeCalendarService.getAllOfficeCalendars(buId, year));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficeCalendarDto> getOfficeCalendarById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                officeCalendarService.getOfficeCalendarById(id));
    }

    @PostMapping
    public ResponseEntity<List<OfficeCalendarDto>> createOfficeCalendars(
            @RequestBody OfficeCalendarDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(officeCalendarService.createOfficeCalendars(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfficeCalendarDto> updateOfficeCalendar(
            @PathVariable Long id,
            @RequestBody OfficeCalendarDto dto) {
        return ResponseEntity.ok(
                officeCalendarService.updateOfficeCalendar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfficeCalendar(
            @PathVariable Long id) {
        officeCalendarService.deleteOfficeCalendar(id);
        return ResponseEntity.noContent().build();
    }
}
