package com.tse.erp.module.asset.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.asset.entity.AssetAssign;
import com.tse.erp.module.asset.entity.AssetItemReturn;
import com.tse.erp.module.asset.repository.AssetAssignRepository;
import com.tse.erp.module.asset.repository.AssetItemReturnRepository;
import com.tse.erp.module.asset.service.AssetItemReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetItemReturnServiceImpl implements AssetItemReturnService {

    private static final List<String> CONDITIONS = List.of(
            "Good", "Fair", "Damaged", "Lost", "Beyond Repair");
    private static final List<String> REMARKS_REQUIRED = List.of(
            "Damaged", "Lost", "Beyond Repair");

    private final AssetItemReturnRepository repository;
    private final AssetAssignRepository assignRepository;

    @Override
    public List<AssetItemReturn> getAll(Long buId, Long emplId) {
        if (buId != null && emplId != null) {
            return repository.findByBuIdAndEmplIdOrderByIdDesc(buId, emplId);
        }
        if (buId != null) {
            return repository.findByBuIdOrderByIdDesc(buId);
        }
        if (emplId != null) {
            return repository.findByEmplIdOrderByIdDesc(emplId);
        }
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public AssetItemReturn getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Return not found with id: " + id));
    }

    // =========================================
    // CREATE
    // =========================================
    @Override
    @Transactional
    public AssetItemReturn create(AssetItemReturn r) {

        if (r.getAssignId() == null) {
            throw new BadRequestException("Asset assignment cannot be empty");
        }
        AssetAssign a = findAssign(r.getAssignId());

        if (!Integer.valueOf(1).equals(a.getStatus())) {
            throw new BadRequestException("Selected assignment is not active");
        }
        if (a.getActualReturnDate() != null
                || repository.findByAssignId(a.getId()).stream().anyMatch(
                x -> Integer.valueOf(1).equals(x.getStatus()))) {
            throw new DuplicateResourceException(
                    "This asset is already returned");
        }

        LocalDate date = cleanDate(r.getActualReturnDate(), a);
        String condition = cleanCondition(r.getConditionOnReturn());
        String remarks = cleanRemarks(r.getRemarks(), condition);

        AssetItemReturn t = new AssetItemReturn();
        t.setBgId(a.getBgId());
        t.setBuId(a.getBuId());
        t.setAssignId(a.getId());
        t.setAssetDate(a.getAssetDate());
        t.setItemId(a.getItemId());
        t.setEmplId(a.getEmplId());
        t.setExpectedReturnDate(a.getExpectedReturnDate());
        t.setActualReturnDate(date);
        t.setConditionOnReturn(condition);
        t.setRemarks(remarks);
        AssetItemReturn saved = repository.save(t);

        a.setActualReturnDate(date);
        assignRepository.save(a);
        return saved;
    }

    // =========================================
    // UPDATE (date, condition, remarks)
    // =========================================
    @Override
    @Transactional
    public AssetItemReturn update(Long id, AssetItemReturn r) {

        AssetItemReturn existing = getById(id);
        AssetAssign a = findAssign(existing.getAssignId());

        LocalDate date = cleanDate(r.getActualReturnDate(), a);
        String condition = cleanCondition(r.getConditionOnReturn());
        String remarks = cleanRemarks(r.getRemarks(), condition);

        existing.setActualReturnDate(date);
        existing.setConditionOnReturn(condition);
        existing.setRemarks(remarks);
        AssetItemReturn saved = repository.save(existing);

        a.setActualReturnDate(date);
        assignRepository.save(a);
        return saved;
    }

    // =========================================
    // DELETE = return undo
    // =========================================
    @Override
    @Transactional
    public void delete(Long id) {

        AssetItemReturn existing = getById(id);
        AssetAssign a = findAssign(existing.getAssignId());

        boolean assignedAgain = assignRepository
                .findByItemIdAndActualReturnDateIsNull(a.getItemId()).stream()
                .anyMatch(x -> Integer.valueOf(1).equals(x.getStatus())
                        && !x.getId().equals(a.getId()));
        if (assignedAgain) {
            throw new DuplicateResourceException(
                    "Cannot undo: this asset is assigned to someone again");
        }

        a.setActualReturnDate(null);
        assignRepository.save(a);
        repository.delete(existing);
    }

    // =========================================
    // HELPERS
    // =========================================
    private AssetAssign findAssign(Long id) {
        return assignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Assignment not found with id: " + id));
    }

    private LocalDate cleanDate(LocalDate d, AssetAssign a) {
        LocalDate today = LocalDate.now();
        LocalDate date = d == null ? today : d;
        if (date.isAfter(today)) {
            throw new BadRequestException(
                    "Actual return date cannot be a future date");
        }
        if (date.isBefore(a.getAssetDate())) {
            throw new BadRequestException(
                    "Actual return date cannot be before assign date");
        }
        return date;
    }

    private String cleanCondition(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException(
                    "Condition on return cannot be empty");
        }
        for (String c : CONDITIONS) {
            if (c.equalsIgnoreCase(v.trim())) {
                return c;
            }
        }
        throw new BadRequestException(
                "Condition on return must be Good, Fair, Damaged, Lost or Beyond Repair");
    }

    private String cleanRemarks(String v, String condition) {
        String remarks = null;
        if (v != null && !v.trim().isEmpty()) {
            remarks = v.trim();
            if (remarks.length() > 500) {
                throw new BadRequestException(
                        "Remarks cannot exceed 500 characters");
            }
            if (remarks.indexOf('<') >= 0 || remarks.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Remarks contain unsupported characters");
            }
        }
        if (REMARKS_REQUIRED.contains(condition) && remarks == null) {
            throw new BadRequestException(
                    "Remarks are required when condition is " + condition);
        }
        return remarks;
    }
}