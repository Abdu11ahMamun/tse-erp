package com.tse.erp.module.asset.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetTransfer;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.repository.AssetLocationRepository;
import com.tse.erp.module.asset.repository.AssetTransferRepository;
import com.tse.erp.module.asset.repository.CustodianDtlRepository;
import com.tse.erp.module.asset.repository.CustodianRepository;
import com.tse.erp.module.asset.service.AssetTransferService;
import com.tse.erp.module.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetTransferServiceImpl implements AssetTransferService {

    private static final List<String> CONDITIONS = List.of(
            "New", "Good", "Fair", "Damaged", "Lost", "Beyond Repair");
    private static final List<String> REMARKS_REQUIRED = List.of(
            "Damaged", "Lost", "Beyond Repair");

    private final AssetTransferRepository repository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final CustodianRepository custodianRepository;
    private final CustodianDtlRepository custodianDtlRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetLocationRepository assetLocationRepository;

    @Override
    public List<AssetTransfer> getAll(Long buId, Long itemId) {
        if (itemId != null) {
            return repository.findByItemIdOrderByIdDesc(itemId);
        }
        if (buId != null) {
            return repository.findByBuIdOrderByIdDesc(buId);
        }
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public AssetTransfer getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Transfer not found with id: " + id));
    }

    @Override
    public AssetTransfer getCurrent(Long itemId) {
        return repository
                .findFirstByItemIdAndStatusOrderByIdDesc(itemId, 1)
                .orElse(null);
    }

    // =========================================
    // CREATE
    // =========================================
    @Override
    @Transactional
    public AssetTransfer create(AssetTransfer r) {

        if (r.getBuId() == null) {
            throw new BadRequestException("Business Unit id cannot be empty");
        }
        var bu = businessUnitRepository.findById(r.getBuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + r.getBuId()));
        active(bu.getStatus(), "Business Unit");

        if (r.getItemId() == null) {
            throw new BadRequestException("Asset item cannot be empty");
        }
        var item = assetItemRepository.findById(r.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Item not found with id: " + r.getItemId()));
        active(item.getStatus(), "Asset Item");

        AssetTransfer t = new AssetTransfer();
        t.setBgId(bu.getBgId());
        t.setBuId(r.getBuId());
        t.setItemId(r.getItemId());

        // FROM: latest transfer-er To, na thakle request theke
        AssetTransfer cur = getCurrent(r.getItemId());
        if (cur != null) {
            t.setFromCustodianId(cur.getToCustodianId());
            t.setFromSubCustodianId(cur.getToSubCustodianId());
            t.setFromDepartmentId(cur.getToDepartmentId());
            t.setFromLocationId(cur.getToLocationId());
            t.setFromAssignDate(cur.getToAssignDate());
            t.setFromConditionOnIssue(cur.getToConditionOnIssue());
            t.setFromRemarks(cur.getToRemarks());
        } else {
            t.setFromCustodianId(r.getFromCustodianId());
            t.setFromSubCustodianId(r.getFromSubCustodianId());
            t.setFromDepartmentId(r.getFromDepartmentId());
            t.setFromLocationId(r.getFromLocationId());
            t.setFromAssignDate(r.getFromAssignDate());
            t.setFromConditionOnIssue(
                    cleanCondition(r.getFromConditionOnIssue(), "From"));
            t.setFromRemarks(cleanText(r.getFromRemarks()));
            if (t.getFromAssignDate() == null) {
                throw new BadRequestException(
                        "From assign date cannot be empty");
            }
            checkRefs(t.getFromCustodianId(), t.getFromSubCustodianId(),
                    t.getFromDepartmentId(), t.getFromLocationId(),
                    bu.getBgId(), "From");
        }

        // TO
        checkRefs(r.getToCustodianId(), r.getToSubCustodianId(),
                r.getToDepartmentId(), r.getToLocationId(),
                bu.getBgId(), "To");

        LocalDate today = LocalDate.now();
        LocalDate toDate =
                r.getToAssignDate() == null ? today : r.getToAssignDate();
        if (toDate.isAfter(today)) {
            throw new BadRequestException(
                    "To assign date cannot be a future date");
        }
        if (toDate.isBefore(t.getFromAssignDate())) {
            throw new BadRequestException(
                    "To assign date cannot be before from assign date");
        }

        String toCondition = cleanCondition(r.getToConditionOnIssue(), "To");
        String toRemarks = cleanText(r.getToRemarks());
        if (REMARKS_REQUIRED.contains(toCondition) && toRemarks == null) {
            throw new BadRequestException(
                    "To remarks are required when condition is "
                            + toCondition);
        }

        t.setToCustodianId(r.getToCustodianId());
        t.setToSubCustodianId(r.getToSubCustodianId());
        t.setToDepartmentId(r.getToDepartmentId());
        t.setToLocationId(r.getToLocationId());
        t.setToAssignDate(toDate);
        t.setToConditionOnIssue(toCondition);
        t.setToRemarks(toRemarks);

        // Kichu na bodlale transfer na
        boolean changed =
                !t.getToCustodianId().equals(t.getFromCustodianId())
                        || !t.getToSubCustodianId().equals(t.getFromSubCustodianId())
                        || !t.getToDepartmentId().equals(t.getFromDepartmentId())
                        || !t.getToLocationId().equals(t.getFromLocationId())
                        || !t.getToConditionOnIssue()
                        .equals(t.getFromConditionOnIssue());
        if (!changed) {
            throw new BadRequestException(
                    "Nothing changed: at least one To value must differ from From");
        }

        // Status default 1, BaseEntity @PrePersist handle korbe
        return repository.save(t);
    }

    // =========================================
    // DELETE = undo (shudhu latest)
    // =========================================
    @Override
    @Transactional
    public void delete(Long id) {
        AssetTransfer existing = getById(id);
        AssetTransfer latest = getCurrent(existing.getItemId());
        if (latest == null || !latest.getId().equals(id)) {
            throw new BadRequestException(
                    "Only the latest transfer of an item can be removed");
        }
        repository.delete(existing);
    }

    // =========================================
    // HELPERS
    // =========================================
    private void checkRefs(Long custodianId, Long subId, Long deptId,
                           Long locationId, Long bgId, String side) {
        if (custodianId == null) {
            throw new BadRequestException(side + " custodian cannot be empty");
        }
        var c = custodianRepository.findById(custodianId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custodian not found with id: " + custodianId));
        active(c.getStatus(), side + " Custodian");

        if (subId == null) {
            throw new BadRequestException(
                    side + " sub custodian cannot be empty");
        }
        var s = custodianDtlRepository.findById(subId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sub Custodian not found with id: " + subId));
        active(s.getStatus(), side + " Sub Custodian");
        // TODO: sub custodian oi custodian-er kina check (CustodianDtl field dekhe)

        if (deptId == null) {
            throw new BadRequestException(side + " department cannot be empty");
        }
        var d = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + deptId));
        active(d.getStatus(), side + " Department");
        if (!bgId.equals(d.getBgId())) {
            throw new BadRequestException(
                    side + " department does not belong to the Business Unit's Group");
        }

        if (locationId == null) {
            throw new BadRequestException(side + " location cannot be empty");
        }
        var l = assetLocationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id: " + locationId));
        active(l.getStatus(), side + " Location");
    }

    private void active(Integer status, String label) {
        if (!Integer.valueOf(1).equals(status)) {
            throw new BadRequestException(
                    "Selected " + label + " is not active");
        }
    }

    private String cleanCondition(String v, String side) {
        if (v == null || v.trim().isEmpty()) {
            throw new BadRequestException(
                    side + " condition on issue cannot be empty");
        }
        for (String c : CONDITIONS) {
            if (c.equalsIgnoreCase(v.trim())) {
                return c;
            }
        }
        throw new BadRequestException(side
                + " condition must be New, Good, Fair, Damaged, Lost or Beyond Repair");
    }

    private String cleanText(String v) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        String t = v.trim();
        if (t.length() > 500) {
            throw new BadRequestException(
                    "Remarks cannot exceed 500 characters");
        }
        if (t.indexOf('<') >= 0 || t.indexOf('>') >= 0) {
            throw new BadRequestException(
                    "Remarks contain unsupported characters");
        }
        return t;
    }
}