package com.tse.erp.module.hr.controller;

import com.tse.erp.module.hr.entity.OfficeNotice;
import com.tse.erp.module.hr.service.OfficeNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/office-notices")
@RequiredArgsConstructor
public class OfficeNoticeController {

    private final OfficeNoticeService officeNoticeService;

    @GetMapping
    public ResponseEntity<List<OfficeNotice>> getAllOfficeNotices(
            @RequestParam(required = false) Long bgId,
            @RequestParam(defaultValue = "false") boolean current) {
        return ResponseEntity.ok(
                officeNoticeService.getAllOfficeNotices(bgId, current));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficeNotice> getOfficeNoticeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                officeNoticeService.getOfficeNoticeById(id));
    }

    @PostMapping
    public ResponseEntity<OfficeNotice> createOfficeNotice(
            @RequestBody OfficeNotice officeNotice) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(officeNoticeService.createOfficeNotice(officeNotice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfficeNotice> updateOfficeNotice(
            @PathVariable Long id,
            @RequestBody OfficeNotice officeNotice) {
        return ResponseEntity.ok(
                officeNoticeService.updateOfficeNotice(id, officeNotice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfficeNotice(@PathVariable Long id) {
        officeNoticeService.deleteOfficeNotice(id);
        return ResponseEntity.noContent().build();
    }
}