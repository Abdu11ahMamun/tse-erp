package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmCalendarMonthDto;
import com.tse.erp.module.accounting.dto.AfmCalendarRequestDto;
import com.tse.erp.module.accounting.dto.AfmCalendarResponseDto;
import com.tse.erp.module.accounting.service.AfmCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-calendars", "/api/v1/accounting-calendars"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmCalendarController {

    private final AfmCalendarService calendarService;

    @GetMapping
    public ResponseEntity<List<AfmCalendarResponseDto>> getAllCalendars(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(calendarService.getAllCalendars(bgId, buId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmCalendarResponseDto> getCalendarById(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.getCalendarById(id));
    }

    @GetMapping("/{id}/months")
    public ResponseEntity<List<AfmCalendarMonthDto>> getCalendarMonths(@PathVariable Long id) {
        return ResponseEntity.ok(calendarService.getCalendarMonths(id));
    }

    @PostMapping
    public ResponseEntity<AfmCalendarResponseDto> createCalendar(
            @RequestBody AfmCalendarRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(calendarService.createCalendar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmCalendarResponseDto> updateCalendar(
            @PathVariable Long id,
            @RequestBody AfmCalendarRequestDto request) {
        return ResponseEntity.ok(calendarService.updateCalendar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{calendarId}/months")
    public ResponseEntity<AfmCalendarMonthDto> addCalendarMonth(
            @PathVariable Long calendarId,
            @RequestBody AfmCalendarMonthDto monthDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(calendarService.addCalendarMonth(calendarId, monthDto));
    }

    @PutMapping("/{calendarId}/months/{monthId}")
    public ResponseEntity<AfmCalendarMonthDto> updateCalendarMonth(
            @PathVariable Long calendarId,
            @PathVariable Long monthId,
            @RequestBody AfmCalendarMonthDto monthDto) {
        return ResponseEntity.ok(calendarService.updateCalendarMonth(calendarId, monthId, monthDto));
    }

    @DeleteMapping("/{calendarId}/months/{monthId}")
    public ResponseEntity<Void> deleteCalendarMonth(
            @PathVariable Long calendarId,
            @PathVariable Long monthId) {
        calendarService.deleteCalendarMonth(calendarId, monthId);
        return ResponseEntity.noContent().build();
    }
}

