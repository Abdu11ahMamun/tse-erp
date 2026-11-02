package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmVoucherAttachDto;
import com.tse.erp.module.accounting.dto.AfmVoucherDetailDto;
import com.tse.erp.module.accounting.dto.AfmVoucherRequestDto;
import com.tse.erp.module.accounting.dto.AfmVoucherResponseDto;
import com.tse.erp.module.accounting.service.AfmVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-vouchers", "/api/v1/afmVouchers", "/api/v1/vouchers"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmVoucherController {

    private final AfmVoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<AfmVoucherResponseDto>> getAllVouchers(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(voucherService.getAllVouchers(bgId, buId, voucherType, status, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmVoucherResponseDto> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @GetMapping("/by-no")
    public ResponseEntity<AfmVoucherResponseDto> getVoucherByNo(
            @RequestParam Long buId,
            @RequestParam String voucherNo) {
        return ResponseEntity.ok(voucherService.getVoucherByNo(buId, voucherNo));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<AfmVoucherDetailDto>> getVoucherDetails(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherDetails(id));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<AfmVoucherAttachDto>> getVoucherAttachments(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherAttachments(id));
    }

    @PostMapping
    public ResponseEntity<AfmVoucherResponseDto> createVoucher(
            @RequestBody AfmVoucherRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(voucherService.createVoucher(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmVoucherResponseDto> updateVoucher(
            @PathVariable Long id,
            @RequestBody AfmVoucherRequestDto request) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    // Detail Sub-resource CRUD
    @PostMapping("/{voucherId}/details")
    public ResponseEntity<AfmVoucherDetailDto> addVoucherDetail(
            @PathVariable Long voucherId,
            @RequestBody AfmVoucherDetailDto detailDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(voucherService.addVoucherDetail(voucherId, detailDto));
    }

    @PutMapping("/{voucherId}/details/{detailId}")
    public ResponseEntity<AfmVoucherDetailDto> updateVoucherDetail(
            @PathVariable Long voucherId,
            @PathVariable Long detailId,
            @RequestBody AfmVoucherDetailDto detailDto) {
        return ResponseEntity.ok(voucherService.updateVoucherDetail(voucherId, detailId, detailDto));
    }

    @DeleteMapping("/{voucherId}/details/{detailId}")
    public ResponseEntity<Void> deleteVoucherDetail(
            @PathVariable Long voucherId,
            @PathVariable Long detailId) {
        voucherService.deleteVoucherDetail(voucherId, detailId);
        return ResponseEntity.noContent().build();
    }

    // Attachment Sub-resource CRUD
    @PostMapping("/{voucherId}/attachments")
    public ResponseEntity<AfmVoucherAttachDto> addVoucherAttachment(
            @PathVariable Long voucherId,
            @RequestBody AfmVoucherAttachDto attachDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(voucherService.addVoucherAttachment(voucherId, attachDto));
    }

    @PutMapping("/{voucherId}/attachments/{attachId}")
    public ResponseEntity<AfmVoucherAttachDto> updateVoucherAttachment(
            @PathVariable Long voucherId,
            @PathVariable Long attachId,
            @RequestBody AfmVoucherAttachDto attachDto) {
        return ResponseEntity.ok(voucherService.updateVoucherAttachment(voucherId, attachId, attachDto));
    }

    @DeleteMapping("/{voucherId}/attachments/{attachId}")
    public ResponseEntity<Void> deleteVoucherAttachment(
            @PathVariable Long voucherId,
            @PathVariable Long attachId) {
        voucherService.deleteVoucherAttachment(voucherId, attachId);
        return ResponseEntity.noContent().build();
    }
}
