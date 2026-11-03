package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetMaintenance;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetMaintenanceRepository;
import com.tse.erp.module.asset.service.AssetMaintenanceService;
import com.tse.erp.module.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetMaintenanceServiceImpl implements AssetMaintenanceService {

    private static final List<String> TYPES = List.of(
            "Preventive Maintenance", "Corrective Maintenance",
            "AMC", "Warranty Service");
    private static final List<String> PRIORITIES = List.of(
            "Low", "Medium", "High", "Critical");
    private static final List<String> END_REQUIRED = List.of(
            "AMC", "Warranty Service");
    private static final List<String> DUE_REQUIRED = List.of(
            "Preventive Maintenance", "AMC");
    private static final BigDecimal MAX_COST =
            new BigDecimal("9999999999999999.99");

    private final AssetMaintenanceRepository repository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public List<AssetMaintenance> getAll(Long buId, Long itemId) {
        if (buId != null && itemId != null) {
            return repository.findByBuIdAndItemIdOrderByIdDesc(buId, itemId);
        }
        if (buId != null) {
            return repository.findByBuIdOrderByIdDesc(buId);
        }
        if (itemId != null) {
            return repository.findByItemIdOrderByIdDesc(itemId);
        }
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public AssetMaintenance getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Maintenance not found with id: " + id));
    }

    @Override
    public AssetMaintenance create(AssetMaintenance r) {
        prepare(r);
        StatusUtil.validate(r.getStatus());
        if (r.getStatus() == null || r.getStatus() == 1) {
            checkOverlap(r, null);
        }
        r.setId(null);
        // Status default 1, BaseEntity @PrePersist handle korbe
        return repository.save(r);
    }

    @Override
    public AssetMaintenance update(Long id, AssetMaintenance r) {
        AssetMaintenance existing = getById(id);
        prepare(r);
        StatusUtil.validate(r.getStatus());

        int effective = r.getStatus() != null
                ? r.getStatus()
                : (existing.getStatus() == null ? 1 : existing.getStatus());
        if (effective == 1) {
            checkOverlap(r, id);
        }

        existing.setBgId(r.getBgId());
        existing.setBuId(r.getBuId());
        existing.setItemId(r.getItemId());
        existing.setMaintenanceType(r.getMaintenanceType());
        existing.setMaintenancePriority(r.getMaintenancePriority());
        existing.setVendorId(r.getVendorId());
        existing.setStartDate(r.getStartDate());
        existing.setEndDate(r.getEndDate());
        existing.setCost(r.getCost());
        existing.setNextDueDate(r.getNextDueDate());
        existing.setRemark(r.getRemark());
        if (r.getStatus() != null) {
            existing.setStatus(r.getStatus());
        }
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.delete(getById(id));
    }

    // =========================================
    // VALIDATE + CLEAN
    // =========================================
    private void prepare(AssetMaintenance r) {

        if (r.getBuId() == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        var bu = businessUnitRepository.findById(r.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + r.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        if (r.getItemId() == null) {
            throw new BadRequestException("Asset item cannot be empty");
        }
        var item = assetItemRepository.findById(r.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + r.getItemId()));
        if (!Integer.valueOf(1).equals(item.getStatus())) {
            throw new BadRequestException(
                    "Selected Asset Item is not active");
        }

        String type = pick(r.getMaintenanceType(), TYPES,
                "Maintenance type",
                "Maintenance type must be Preventive Maintenance, "
                        + "Corrective Maintenance, AMC or Warranty Service");
        String priority = pick(r.getMaintenancePriority(), PRIORITIES,
                "Maintenance priority",
                "Maintenance priority must be Low, Medium, High or Critical");

        if (r.getVendorId() == null) {
            throw new BadRequestException("Vendor cannot be empty");
        }
        var vendor = supplierRepository.findById(r.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found with id: " + r.getVendorId()));
        if (!Integer.valueOf(1).equals(vendor.getStatus())) {
            throw new BadRequestException("Selected Vendor is not active");
        }

        // Dates
        LocalDate start = r.getStartDate();
        if (start == null) {
            throw new BadRequestException("Start date cannot be empty");
        }
        if (END_REQUIRED.contains(type) && r.getEndDate() == null) {
            throw new BadRequestException(
                    "End date is required for " + type);
        }
        if (r.getEndDate() != null && r.getEndDate().isBefore(start)) {
            throw new BadRequestException(
                    "End date cannot be before start date");
        }
        if (DUE_REQUIRED.contains(type) && r.getNextDueDate() == null) {
            throw new BadRequestException(
                    "Next due date is required for " + type);
        }
        if (r.getNextDueDate() != null && r.getNextDueDate().isBefore(start)) {
            throw new BadRequestException(
                    "Next due date cannot be before start date");
        }

        // Cost
        BigDecimal cost = r.getCost();
        if (cost == null) {
            throw new BadRequestException("Cost cannot be empty");
        }
        if (cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Cost cannot be negative");
        }
        if (cost.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(
                    "Cost allows maximum 2 decimal places");
        }
        if (cost.compareTo(MAX_COST) > 0) {
            throw new BadRequestException("Cost is too large");
        }

        // Remark
        String remark = null;
        if (r.getRemark() != null && !r.getRemark().trim().isEmpty()) {
            remark = r.getRemark().trim();
            if (remark.length() > 500) {
                throw new BadRequestException(
                        "Remark cannot exceed 500 characters");
            }
            if (remark.indexOf('<') >= 0 || remark.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Remark contains unsupported characters");
            }
        }

        r.setBgId(bu.getBgId());
        r.setMaintenanceType(type);
        r.setMaintenancePriority(priority);
        r.setCost(cost.setScale(2, RoundingMode.HALF_UP));
        r.setRemark(remark);
    }

    private String pick(String v, List<String> allowed,
                        String label, String message) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException(label + " cannot be empty");
        }
        for (String a : allowed) {
            if (a.equalsIgnoreCase(v.trim())) {
                return a;
            }
        }
        throw new BadRequestException(message);
    }

    // Same item + same type + active record-er date range overlap
    private void checkOverlap(AssetMaintenance r, Long selfId) {
        LocalDate s = r.getStartDate();
        LocalDate e = r.getEndDate() == null ? s : r.getEndDate();

        boolean overlap = repository
                .findByItemIdAndMaintenanceType(
                        r.getItemId(), r.getMaintenanceType())
                .stream()
                .filter(x -> Integer.valueOf(1).equals(x.getStatus()))
                .filter(x -> selfId == null || !x.getId().equals(selfId))
                .anyMatch(x -> {
                    LocalDate xs = x.getStartDate();
                    LocalDate xe = x.getEndDate() == null
                            ? xs : x.getEndDate();
                    return !s.isAfter(xe) && !e.isBefore(xs);
                });
        if (overlap) {
            throw new DuplicateResourceException(
                    "An active " + r.getMaintenanceType()
                            + " record already overlaps this period for the asset");
        }
    }
}