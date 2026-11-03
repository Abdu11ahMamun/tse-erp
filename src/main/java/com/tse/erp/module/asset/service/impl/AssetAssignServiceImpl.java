package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetAssign;
import com.tse.erp.module.asset.entity.AssetItem;
import com.tse.erp.module.asset.repository.AssetAssignRepository;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.service.AssetAssignService;
import com.tse.erp.module.hr.entity.Employee;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.asset.repository.AssetItemReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AssetAssignServiceImpl implements AssetAssignService {

    private static final Set<String> CONDITIONS =
            Set.of("New", "Good", "Fair", "Damaged");

    private final AssetAssignRepository repository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetItemReturnRepository assetItemReturnRepository;

    // =========================================
    // READ
    // =========================================
    @Override
    public List<AssetAssign> getAll(
            Long buId, Long emplId, boolean openOnly) {

        List<AssetAssign> list;
        if (buId != null && emplId != null) {
            list = repository.findByBuIdAndEmplIdOrderByIdDesc(buId, emplId);
        } else if (buId != null) {
            list = repository.findByBuIdOrderByIdDesc(buId);
        } else if (emplId != null) {
            list = repository.findByEmplIdOrderByIdDesc(emplId);
        } else {
            list = repository.findAllByOrderByIdDesc();
        }

        if (!openOnly) {
            return list;
        }
        return list.stream()
                .filter(a -> a.getActualReturnDate() == null)
                .toList();
    }

    @Override
    public AssetAssign getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Assignment not found with id: " + id));
    }

    // =========================================
    // CREATE
    // =========================================
    @Override
    @Transactional
    public AssetAssign create(AssetAssign r) {

        prepare(r);
        StatusUtil.validate(r.getStatus());

        int effective = r.getStatus() == null ? 1 : r.getStatus();
        if (effective == 1) {
            checkAvailable(r.getItemId(), null);
        }

        r.setId(null);
        // Return date create-e set kora jay na
        r.setActualReturnDate(null);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return repository.save(r);
    }

    // =========================================
    // UPDATE
    // =========================================
    @Override
    @Transactional
    public AssetAssign update(Long id, AssetAssign r) {

        AssetAssign existing = getById(id);
        if (existing.getActualReturnDate() != null) {
            throw new BadRequestException(
                    "Returned assignment cannot be edited");
        }

        prepare(r);
        StatusUtil.validate(r.getStatus());

        int effective = r.getStatus() != null
                ? r.getStatus()
                : (existing.getStatus() == null ? 1 : existing.getStatus());
        if (effective == 1) {
            checkAvailable(r.getItemId(), id);
        }

        existing.setBgId(r.getBgId());
        existing.setBuId(r.getBuId());
        existing.setAssetDate(r.getAssetDate());
        existing.setItemId(r.getItemId());
        existing.setEmplId(r.getEmplId());
        existing.setExpectedReturnDate(r.getExpectedReturnDate());
        existing.setConditionOnIssue(r.getConditionOnIssue());
        existing.setRemarks(r.getRemarks());
        if (r.getStatus() != null) {
            existing.setStatus(r.getStatus());
        }
        return repository.save(existing);
    }

    // =========================================
    // RETURN
    // =========================================
    @Override
    @Transactional
    public AssetAssign markReturned(Long id, LocalDate returnDate) {

        AssetAssign existing = getById(id);
        if (existing.getActualReturnDate() != null) {
            throw new BadRequestException("Asset is already returned");
        }

        LocalDate date = returnDate == null ? LocalDate.now() : returnDate;
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "Return date cannot be a future date");
        }
        if (date.isBefore(existing.getAssetDate())) {
            throw new BadRequestException(
                    "Return date cannot be before assign date");
        }

        existing.setActualReturnDate(date);
        return repository.save(existing);
    }

    // =========================================
    // DELETE
    // =========================================
    @Override
    public void delete(Long id) {
        AssetAssign existing = getById(id);
        if (assetItemReturnRepository.existsByAssignId(id)) {
            throw new BadRequestException(
                    "Cannot delete: a return record exists for this assignment");
        }
        repository.delete(existing);
    }

    // =========================================
    // VALIDATE + CLEAN
    // =========================================
    private void prepare(AssetAssign r) {

        // Business Unit (BG BU theke ashe)
        if (r.getBuId() == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        BusinessUnit bu = businessUnitRepository.findById(r.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + r.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        // Asset Item
        if (r.getItemId() == null) {
            throw new BadRequestException("Asset item cannot be empty");
        }
        AssetItem item = assetItemRepository.findById(r.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + r.getItemId()));
        if (!Integer.valueOf(1).equals(item.getStatus())) {
            throw new BadRequestException(
                    "Selected Asset Item is not active");
        }

        // Employee
        if (r.getEmplId() == null) {
            throw new BadRequestException("Employee cannot be empty");
        }
        Employee emp = employeeRepository.findById(r.getEmplId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + r.getEmplId()));
        if (!Integer.valueOf(1).equals(emp.getStatus())) {
            throw new BadRequestException(
                    "Selected Employee is not active");
        }
        if (!bu.getBgId().equals(emp.getBgId())) {
            throw new BadRequestException(
                    "Employee does not belong to the Business Unit's Group");
        }

        // Assign date (na dile aaj)
        LocalDate today = LocalDate.now();
        LocalDate assignDate =
                r.getAssetDate() == null ? today : r.getAssetDate();
        if (assignDate.isAfter(today)) {
            throw new BadRequestException(
                    "Assign date cannot be a future date");
        }

        // Expected return (optional)
        if (r.getExpectedReturnDate() != null
                && r.getExpectedReturnDate().isBefore(assignDate)) {
            throw new BadRequestException(
                    "Expected return date cannot be before assign date");
        }

        // Condition on issue
        if (r.getConditionOnIssue() == null
                || r.getConditionOnIssue().trim().isEmpty()) {
            throw new BadRequestException(
                    "Condition on issue cannot be empty");
        }
        String condition = null;
        for (String c : CONDITIONS) {
            if (c.equalsIgnoreCase(r.getConditionOnIssue().trim())) {
                condition = c;
                break;
            }
        }
        if (condition == null) {
            throw new BadRequestException(
                    "Condition on issue must be New, Good, Fair or Damaged");
        }

        // Remarks (Damaged hole required)
        String remarks = null;
        if (r.getRemarks() != null && !r.getRemarks().trim().isEmpty()) {
            remarks = r.getRemarks().trim();
            if (remarks.length() > 500) {
                throw new BadRequestException(
                        "Remarks cannot exceed 500 characters");
            }
            if (remarks.indexOf('<') >= 0 || remarks.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Remarks contain unsupported characters");
            }
        }
        if ("Damaged".equals(condition) && remarks == null) {
            throw new BadRequestException(
                    "Remarks are required when condition is Damaged");
        }

        r.setBgId(bu.getBgId());
        r.setAssetDate(assignDate);
        r.setConditionOnIssue(condition);
        r.setRemarks(remarks);
    }

    // Item onno kothao open assign thakle block
    private void checkAvailable(Long itemId, Long selfId) {
        boolean taken = repository
                .findByItemIdAndActualReturnDateIsNull(itemId).stream()
                .anyMatch(x -> Integer.valueOf(1).equals(x.getStatus())
                        && (selfId == null || !x.getId().equals(selfId)));
        if (taken) {
            throw new DuplicateResourceException(
                    "This asset is already assigned to an employee");
        }
    }
}