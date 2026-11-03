package com.tse.erp.module.asset.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.dto.GatePassDto;
import com.tse.erp.module.asset.dto.GatePassItemDto;
import com.tse.erp.module.asset.entity.GatePass;
import com.tse.erp.module.asset.entity.GatePassDtl;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.CustodianRepository;
import com.tse.erp.module.asset.repository.GatePassDtlRepository;
import com.tse.erp.module.asset.repository.GatePassRepository;
import com.tse.erp.module.asset.service.GatePassService;
import com.tse.erp.module.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GatePassServiceImpl implements GatePassService {

    private final GatePassRepository repository;
    private final GatePassDtlRepository dtlRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final SupplierRepository supplierRepository;
    private final CustodianRepository custodianRepository;
    private final AssetItemRepository assetItemRepository;

    @Override
    public List<GatePassDto> getAll(Long buId) {
        List<GatePass> list = buId == null
                ? repository.findAllByOrderByIdDesc()
                : repository.findByBuIdOrderByIdDesc(buId);
        return list.stream().map(g -> toDto(g, false)).toList();
    }

    @Override
    public GatePassDto getById(Long id) {
        return toDto(find(id), true);
    }

    // =========================================
    // CREATE
    // =========================================
    @Override
    @Transactional
    public GatePassDto create(GatePassDto dto) {

        StatusUtil.validate(dto.getStatus());
        var bu = validateHeader(dto);
        validateItems(dto.getItems());

        GatePass g = new GatePass();
        apply(g, dto, bu.getBgId());
        if (dto.getStatus() != null) {
            g.setStatus(dto.getStatus());
        }
        GatePass saved = repository.save(g);
        saved.setGatePassNo("GP-" + String.format("%06d", saved.getId()));
        repository.save(saved);

        List<GatePassDtl> rows = new ArrayList<>();
        for (GatePassItemDto i : dto.getItems()) {
            GatePassDtl d = new GatePassDtl();
            d.setGatePassId(saved.getId());
            d.setItemId(i.getItemId());
            d.setQuantity(i.getQuantity());
            if (i.getStatus() != null) {
                d.setStatus(i.getStatus());
            }
            rows.add(d);
        }
        dtlRepository.saveAll(rows);
        return toDto(saved, true);
    }

    // =========================================
    // UPDATE
    // =========================================
    @Override
    @Transactional
    public GatePassDto update(Long id, GatePassDto dto) {

        GatePass g = find(id);
        StatusUtil.validate(dto.getStatus());
        var bu = validateHeader(dto);
        validateItems(dto.getItems());

        apply(g, dto, bu.getBgId());
        if (dto.getStatus() != null) {
            g.setStatus(dto.getStatus());
        }
        repository.save(g);

        List<GatePassDtl> current =
                dtlRepository.findByGatePassIdOrderByIdAsc(id);
        Map<Long, GatePassDtl> byId = new HashMap<>();
        Map<Long, GatePassDtl> byItem = new HashMap<>();
        for (GatePassDtl d : current) {
            byId.put(d.getId(), d);
            byItem.put(d.getItemId(), d);
        }

        Set<Long> kept = new HashSet<>();
        List<GatePassDtl> toSave = new ArrayList<>();
        for (GatePassItemDto i : dto.getItems()) {
            GatePassDtl d;
            if (i.getId() != null) {
                d = byId.get(i.getId());
                if (d == null) {
                    throw new BadRequestException("Row id " + i.getId()
                            + " does not belong to this Gate Pass");
                }
            } else if (byItem.containsKey(i.getItemId())) {
                // Purono soft-removed row-ke reactivate
                d = byItem.get(i.getItemId());
            } else {
                d = new GatePassDtl();
                d.setGatePassId(id);
            }
            d.setItemId(i.getItemId());
            d.setQuantity(i.getQuantity());
            d.setStatus(i.getStatus() != null ? i.getStatus() : 1);
            if (d.getId() != null) {
                kept.add(d.getId());
            }
            toSave.add(d);
        }
        for (GatePassDtl d : current) {
            if (!kept.contains(d.getId())) {
                d.setStatus(0);
                toSave.add(d);
            }
        }
        dtlRepository.saveAll(toSave);
        return toDto(g, true);
    }

    // =========================================
    // DELETE
    // =========================================
    @Override
    @Transactional
    public void delete(Long id) {
        GatePass g = find(id);
        dtlRepository.deleteAll(
                dtlRepository.findByGatePassIdOrderByIdAsc(id));
        repository.delete(g);
    }

    // =========================================
    // HELPERS
    // =========================================
    private GatePass find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Gate Pass not found with id: " + id));
    }

    private com.tse.erp.module.admin.entity.BusinessUnit validateHeader(
            GatePassDto d) {

        if (d.getBuId() == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        var bu = businessUnitRepository.findById(d.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + d.getBuId()));
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }

        LocalDate today = LocalDate.now();
        LocalDate date = d.getGatePassDate() == null
                ? today : d.getGatePassDate();
        if (date.isAfter(today)) {
            throw new BadRequestException(
                    "Gate pass date cannot be a future date");
        }
        d.setGatePassDate(date);

        if (d.getSupplierId() == null) {
            throw new BadRequestException("Supplier cannot be empty");
        }
        var s = supplierRepository.findById(d.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with id: " + d.getSupplierId()));
        if (!Integer.valueOf(1).equals(s.getStatus())) {
            throw new BadRequestException("Selected Supplier is not active");
        }

        if (d.getCustodianId() == null) {
            throw new BadRequestException("Custodian cannot be empty");
        }
        var c = custodianRepository.findById(d.getCustodianId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodian not found with id: " + d.getCustodianId()));
        if (!Integer.valueOf(1).equals(c.getStatus())) {
            throw new BadRequestException("Selected Custodian is not active");
        }

        if (d.getRemark() != null && !d.getRemark().trim().isEmpty()) {
            String r = d.getRemark().trim();
            if (r.length() > 500) {
                throw new BadRequestException(
                        "Remark cannot exceed 500 characters");
            }
            if (r.indexOf('<') >= 0 || r.indexOf('>') >= 0) {
                throw new BadRequestException(
                        "Remark contains unsupported characters");
            }
            d.setRemark(r);
        } else {
            d.setRemark(null);
        }
        return bu;
    }

    private void validateItems(List<GatePassItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("At least one item is required");
        }
        Set<Long> seen = new HashSet<>();
        for (GatePassItemDto i : items) {
            StatusUtil.validate(i.getStatus());
            if (i.getItemId() == null) {
                throw new BadRequestException("Asset item cannot be empty");
            }
            var item = assetItemRepository.findById(i.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Asset Item not found with id: " + i.getItemId()));
            if (!Integer.valueOf(1).equals(item.getStatus())) {
                throw new BadRequestException(
                        "Selected Asset Item is not active: " + i.getItemId());
            }
            if (i.getQuantity() == null) {
                throw new BadRequestException("Quantity cannot be empty");
            }
            if (i.getQuantity() < 1 || i.getQuantity() > 1_000_000) {
                throw new BadRequestException(
                        "Quantity must be between 1 and 1000000");
            }
            if (!seen.add(i.getItemId())) {
                throw new DuplicateResourceException(
                        "Duplicate item in request: " + i.getItemId());
            }
        }
    }

    private void apply(GatePass g, GatePassDto d, Long bgId) {
        g.setBgId(bgId);
        g.setBuId(d.getBuId());
        g.setGatePassDate(d.getGatePassDate());
        g.setSupplierId(d.getSupplierId());
        g.setCustodianId(d.getCustodianId());
        g.setRemark(d.getRemark());
    }

    private GatePassDto toDto(GatePass g, boolean withItems) {
        GatePassDto d = new GatePassDto();
        d.setId(g.getId());
        d.setBgId(g.getBgId());
        d.setBuId(g.getBuId());
        d.setGatePassDate(g.getGatePassDate());
        d.setGatePassNo(g.getGatePassNo());
        d.setSupplierId(g.getSupplierId());
        d.setCustodianId(g.getCustodianId());
        d.setRemark(g.getRemark());
        d.setStatus(g.getStatus());
        if (withItems) {
            List<GatePassItemDto> items = new ArrayList<>();
            for (GatePassDtl r : dtlRepository
                    .findByGatePassIdOrderByIdAsc(g.getId())) {
                if (!Integer.valueOf(1).equals(r.getStatus())) {
                    continue;
                }
                GatePassItemDto i = new GatePassItemDto();
                i.setId(r.getId());
                i.setItemId(r.getItemId());
                i.setQuantity(r.getQuantity());
                i.setStatus(r.getStatus());
                items.add(i);
            }
            d.setItems(items);
        }
        return d;
    }
}