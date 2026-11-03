package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetRepair;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetRepairRepository;
import com.tse.erp.module.asset.service.AssetRepairService;
import com.tse.erp.module.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetRepairServiceImpl implements AssetRepairService {

    private final AssetRepairRepository repository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final SupplierRepository supplierRepository;

    @Override
    public List<AssetRepair> getAll(
            Long buId, Long itemId, boolean openOnly) {
        List<AssetRepair> list;
        if (buId != null && itemId != null) {
            list = repository.findByBuIdAndItemIdOrderByIdDesc(buId, itemId);
        } else if (buId != null) {
            list = repository.findByBuIdOrderByIdDesc(buId);
        } else if (itemId != null) {
            list = repository.findByItemIdOrderByIdDesc(itemId);
        } else {
            list = repository.findAllByOrderByIdDesc();
        }
        if (!openOnly) {
            return list;
        }
        return list.stream()
                .filter(x -> x.getDeliveryDate() == null)
                .toList();
    }

    @Override
    public AssetRepair getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Repair not found with id: " + id));
    }

    @Override
    @Transactional
    public AssetRepair create(AssetRepair r) {
        prepare(r);
        StatusUtil.validate(r.getStatus());
        if (r.getStatus() == null || r.getStatus() == 1) {
            checkOpen(r, null);
        }
        r.setId(null);
        // Status default 1, BaseEntity @PrePersist handle korbe
        return repository.save(r);
    }

    @Override
    @Transactional
    public AssetRepair update(Long id, AssetRepair r) {
        AssetRepair existing = getById(id);
        prepare(r);
        StatusUtil.validate(r.getStatus());

        int effective = r.getStatus() != null
                ? r.getStatus()
                : (existing.getStatus() == null ? 1 : existing.getStatus());
        if (effective == 1) {
            checkOpen(r, id);
        }

        existing.setBgId(r.getBgId());
        existing.setBuId(r.getBuId());
        existing.setItemId(r.getItemId());
        existing.setReceiveDate(r.getReceiveDate());
        existing.setReceiveId(r.getReceiveId());
        existing.setProbSpec(r.getProbSpec());
        existing.setSupportEngineer(r.getSupportEngineer());
        existing.setStartDate(r.getStartDate());
        existing.setFinishDate(r.getFinishDate());
        existing.setDeliveryDate(r.getDeliveryDate());
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
    private void prepare(AssetRepair r) {

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

        if (r.getReceiveId() == null) {
            throw new BadRequestException("Receive by (vendor) cannot be empty");
        }
        var vendor = supplierRepository.findById(r.getReceiveId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found with id: " + r.getReceiveId()));
        if (!Integer.valueOf(1).equals(vendor.getStatus())) {
            throw new BadRequestException("Selected Vendor is not active");
        }

        // Dates
        LocalDate today = LocalDate.now();
        LocalDate receive = r.getReceiveDate() == null
                ? today : r.getReceiveDate();
        if (receive.isAfter(today)) {
            throw new BadRequestException(
                    "Receive date cannot be a future date");
        }
        if (r.getStartDate() == null) {
            throw new BadRequestException("Start date cannot be empty");
        }
        if (r.getStartDate().isBefore(receive)) {
            throw new BadRequestException(
                    "Start date cannot be before receive date");
        }
        if (r.getFinishDate() != null
                && r.getFinishDate().isBefore(r.getStartDate())) {
            throw new BadRequestException(
                    "Finish date cannot be before start date");
        }
        if (r.getDeliveryDate() != null) {
            if (r.getFinishDate() == null) {
                throw new BadRequestException(
                        "Finish date is required before delivery date");
            }
            if (r.getDeliveryDate().isBefore(receive)
                    || r.getDeliveryDate().isBefore(r.getFinishDate())) {
                throw new BadRequestException(
                        "Delivery date cannot be before receive or finish date");
            }
        }

        // Problem spec
        if (r.getProbSpec() == null || r.getProbSpec().trim().isEmpty()) {
            throw new BadRequestException(
                    "Problem specification cannot be empty");
        }
        String prob = r.getProbSpec().trim();
        if (prob.length() > 1000) {
            throw new BadRequestException(
                    "Problem specification cannot exceed 1000 characters");
        }
        if (prob.indexOf('<') >= 0 || prob.indexOf('>') >= 0) {
            throw new BadRequestException(
                    "Problem specification contains unsupported characters");
        }

        // Support engineer
        if (r.getSupportEngineer() == null
                || r.getSupportEngineer().trim().isEmpty()) {
            throw new BadRequestException("Support engineer cannot be empty");
        }
        String engineer = r.getSupportEngineer().trim();
        if (engineer.length() < 2 || engineer.length() > 100) {
            throw new BadRequestException(
                    "Support engineer must be between 2 and 100 characters");
        }
        if (engineer.indexOf('<') >= 0 || engineer.indexOf('>') >= 0) {
            throw new BadRequestException(
                    "Support engineer contains unsupported characters");
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
        r.setReceiveDate(receive);
        r.setProbSpec(prob);
        r.setSupportEngineer(engineer);
        r.setRemark(remark);
    }

    // Item-er onno open (delivery hoy nai) active repair thakle block
    private void checkOpen(AssetRepair r, Long selfId) {
        if (r.getDeliveryDate() != null) {
            return;
        }
        boolean open = repository
                .findByItemIdAndDeliveryDateIsNull(r.getItemId()).stream()
                .anyMatch(x -> Integer.valueOf(1).equals(x.getStatus())
                        && (selfId == null || !x.getId().equals(selfId)));
        if (open) {
            throw new DuplicateResourceException(
                    "This asset already has an open repair record");
        }
    }
}