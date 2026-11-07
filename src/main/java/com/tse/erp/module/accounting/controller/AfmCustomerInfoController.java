package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.dto.AfmCustomerInfoRequestDto;
import com.tse.erp.module.accounting.dto.AfmCustomerInfoResponseDto;
import com.tse.erp.module.accounting.service.AfmCustomerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/afm-customers")
@RequiredArgsConstructor
public class AfmCustomerInfoController {

    private final AfmCustomerInfoService customerInfoService;

    @GetMapping
    public ResponseEntity<List<AfmCustomerInfoResponseDto>> getAllCustomers(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) Integer status
    ) {
        return ResponseEntity.ok(
                customerInfoService.getAllCustomers(bgId, buId, query, customerType, status)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmCustomerInfoResponseDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerInfoService.getCustomerById(id));
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextCustomerCode(@RequestParam Long buId) {
        String nextCode = customerInfoService.generateNextCustomerCode(buId);
        return ResponseEntity.ok(Collections.singletonMap("customerCode", nextCode));
    }

    @PostMapping
    public ResponseEntity<AfmCustomerInfoResponseDto> createCustomer(
            @RequestBody AfmCustomerInfoRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerInfoService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmCustomerInfoResponseDto> updateCustomer(
            @PathVariable Long id,
            @RequestBody AfmCustomerInfoRequestDto request
    ) {
        return ResponseEntity.ok(customerInfoService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerInfoService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

