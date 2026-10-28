package com.tse.erp.module.accounting.controller;

import com.tse.erp.module.accounting.entity.AfmChartOfAccounts;
import com.tse.erp.module.accounting.service.AfmChartOfAccountsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/afm-chart-of-accounts", "/api/v1/afmChartOfAccounts", "/api/v1/chart-of-accounts"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AfmChartOfAccountsController {

    private final AfmChartOfAccountsService service;

    @GetMapping
    public ResponseEntity<List<AfmChartOfAccounts>> getAllAccounts(
            @RequestParam(required = false) Long bgId,
            @RequestParam(required = false) Long buId) {
        return ResponseEntity.ok(service.getAllAccounts(bgId, buId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AfmChartOfAccounts> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAccountById(id));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<AfmChartOfAccounts>> getAccountsByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(service.getAccountsByParentId(parentId));
    }

    @PostMapping
    public ResponseEntity<AfmChartOfAccounts> createAccount(
            @RequestBody AfmChartOfAccounts account) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createAccount(account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AfmChartOfAccounts> updateAccount(
            @PathVariable Long id,
            @RequestBody AfmChartOfAccounts account) {
        return ResponseEntity.ok(service.updateAccount(id, account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        service.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
