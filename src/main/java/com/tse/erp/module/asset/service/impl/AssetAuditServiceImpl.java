package com.tse.erp.module.asset.service.impl;

import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.asset.entity.AssetAudit;
import com.tse.erp.module.asset.repository.AssetAssignRepository;
import com.tse.erp.module.asset.repository.AssetAuditRepository;
import com.tse.erp.module.asset.repository.AssetItemRepository;
import com.tse.erp.module.asset.service.AssetAuditService;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.security.AuthUser;
import com.tse.erp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AssetAuditServiceImpl implements AssetAuditService {

    private static final List<String> TYPES = List.of(
            "Sale", "Scrap", "Donation", "Write-off", "Lost", "Retirement");
    private static final List<String> REMARK_REQUIRED = List.of(
            "Write-off", "Lost");
    private static final List<String> STATUSES = List.of(
            "DRAFT", "SUBMITTED", "APPROVED", "REJECTED",
            "CANCELLED", "COMPLETED");
    private static final List<String> BLOCKING = List.of(
            "DRAFT", "SUBMITTED", "APPROVED", "COMPLETED");
    private static final BigDecimal MAX =
            new BigDecimal("9999999999999999.99");

    private final AssetAuditRepository repository;
    private final BusinessUnitRepository businessUnitRepository;
    private final AssetItemRepository assetItemRepository;
    private final AssetAssignRepository assetAssignRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<AssetAudit> getAll(Long buId, String disposalStatus) {
        String st = (disposalStatus == null || disposalStatus.isBlank())
                ? null : disposalStatus.trim().toUpperCase(Locale.ROOT);
        if (st != null && !STATUSES.contains(st)) {
            throw new BadRequestException("Invalid status: " + st);
        }
        if (buId != null && st != null) {
            return repository
                    .findByBuIdAndDisposalStatusOrderByIdDesc(buId, st);
        }
        if (buId != null) {
            return repository.findByBuIdOrderByIdDesc(buId);
        }
        if (st != null) {
            return repository.findByDisposalStatusOrderByIdDesc(st);
        }
        return repository.findAllByOrderByIdDesc();
    }

    @Override
    public AssetAudit getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset Audit not found with id: " + id));
    }

    @Override
    @Transactional
    public AssetAudit create(AssetAudit r) {
        String st = r.getDisposalStatus() == null
                || r.getDisposalStatus().isBlank()
                ? "DRAFT" : r.getDisposalStatus().trim().toUpperCase(Locale.ROOT);
        if (!st.equals("DRAFT") && !st.equals("SUBMITTED")) {
            throw new BadRequestException(
                    "A new record can only be DRAFT or SUBMITTED");
        }
        prepare(r, null);
        r.setId(null);
        r.setApprovedBy(null);
        r.setDisposalStatus(st);
        return repository.save(r);
    }

    @Override
    @Transactional
    public AssetAudit update(Long id, AssetAudit r) {
        AssetAudit existing = getById(id);
        requireStatus(existing, "DRAFT");
        prepare(r, id);

        existing.setBgId(r.getBgId());
        existing.setBuId(r.getBuId());
        existing.setItemId(r.getItemId());
        existing.setDisposalDate(r.getDisposalDate());
        existing.setDisposalType(r.getDisposalType());
        existing.setDisposalValue(r.getDisposalValue());
        existing.setWrittenDownValue(r.getWrittenDownValue());
        existing.setGainOnDisposal(r.getGainOnDisposal());
        existing.setLossOnDisposal(r.getLossOnDisposal());
        existing.setReason(r.getReason());
        existing.setRemark(r.getRemark());
        return repository.save(existing);
    }

    @Override
    @Transactional
    public AssetAudit submit(Long id) {
        AssetAudit d = getById(id);
        requireStatus(d, "DRAFT");
        d.setDisposalStatus("SUBMITTED");
        return repository.save(d);
    }

    @Override
    @Transactional
    public AssetAudit approve(Long id, Long approvedBy) {
        AssetAudit d = getById(id);
        requireStatus(d, "SUBMITTED");

        Long approver = approvedBy != null
                ? approvedBy
                : CurrentUser.get().map(AuthUser::empId).orElse(null);
        if (approver == null) {
            throw new BadRequestException(
                    "Approver is required (your user has no Employee, pass approvedBy)");
        }
        var emp = employeeRepository.findById(approver)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Approver not found with id: " + approver));
        if (!Integer.valueOf(1).equals(emp.getStatus())) {
            throw new BadRequestException("Selected Approver is not active");
        }
        // TODO: approver permission (RBAC ashle)

        d.setApprovedBy(approver);
        d.setDisposalStatus("APPROVED");
        return repository.save(d);
    }

    @Override
    @Transactional
    public AssetAudit reject(Long id) {
        AssetAudit d = getById(id);
        requireStatus(d, "SUBMITTED");
        d.setDisposalStatus("REJECTED");
        return repository.save(d);
    }

    @Override
    @Transactional
    public AssetAudit cancel(Long id) {
        AssetAudit d = getById(id);
        requireStatus(d, "DRAFT", "SUBMITTED");
        d.setDisposalStatus("CANCELLED");
        return repository.save(d);
    }

    @Override
    @Transactional
    public AssetAudit complete(Long id) {
        AssetAudit d = getById(id);
        requireStatus(d, "APPROVED");
        d.setDisposalStatus("COMPLETED");
        return repository.save(d);
    }

    @Override
    public void delete(Long id) {
        AssetAudit d = getById(id);
        requireStatus(d, "DRAFT");
        repository.delete(d);
    }

    // =========================================
    // VALIDATE + CALCULATE
    // =========================================
    private void prepare(AssetAudit r, Long selfId) {

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
        boolean assigned = assetAssignRepository
                .findByItemIdAndActualReturnDateIsNull(r.getItemId())
                .stream()
                .anyMatch(a -> Integer.valueOf(1).equals(a.getStatus()));
        if (assigned) {
            throw new BadRequestException(
                    "Asset is currently assigned to an employee, return it first");
        }
        boolean existing = repository.findByItemId(r.getItemId()).stream()
                .filter(x -> selfId == null || !x.getId().equals(selfId))
                .anyMatch(x -> BLOCKING.contains(x.getDisposalStatus()));
        if (existing) {
            throw new DuplicateResourceException(
                    "A record already exists for this asset");
        }

        LocalDate date = r.getDisposalDate() == null
                ? LocalDate.now() : r.getDisposalDate();
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "Disposal date cannot be a future date");
        }

        if (r.getDisposalType() == null || r.getDisposalType().isBlank()) {
            throw new BadRequestException("Disposal type cannot be empty");
        }
        String type = null;
        for (String t : TYPES) {
            if (t.equalsIgnoreCase(r.getDisposalType().trim())) {
                type = t;
                break;
            }
        }
        if (type == null) {
            throw new BadRequestException(
                    "Disposal type must be Sale, Scrap, Donation, Write-off, Lost or Retirement");
        }

        if (r.getReason() == null || r.getReason().trim().isEmpty()) {
            throw new BadRequestException("Reason cannot be empty");
        }
        String reason = r.getReason().trim();
        if (reason.length() < 2 || reason.length() > 100) {
            throw new BadRequestException(
                    "Reason must be between 2 and 100 characters");
        }
        if (reason.indexOf('<') >= 0 || reason.indexOf('>') >= 0) {
            throw new BadRequestException(
                    "Reason contains unsupported characters");
        }

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
        if (REMARK_REQUIRED.contains(type) && remark == null) {
            throw new BadRequestException(
                    "Remark is required for disposal type " + type);
        }

        BigDecimal value = money(r.getDisposalValue(), "Disposal value");
        BigDecimal wdv = money(r.getWrittenDownValue(), "Written down value");
        BigDecimal diff = value.subtract(wdv);
        BigDecimal gain = diff.compareTo(BigDecimal.ZERO) > 0
                ? diff : BigDecimal.ZERO;
        BigDecimal loss = diff.compareTo(BigDecimal.ZERO) < 0
                ? diff.negate() : BigDecimal.ZERO;

        r.setBgId(bu.getBgId());
        r.setDisposalDate(date);
        r.setDisposalType(type);
        r.setReason(reason);
        r.setRemark(remark);
        r.setDisposalValue(value);
        r.setWrittenDownValue(wdv);
        r.setGainOnDisposal(gain.setScale(2, RoundingMode.HALF_UP));
        r.setLossOnDisposal(loss.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal money(BigDecimal v, String label) {
        if (v == null) {
            throw new BadRequestException(label + " cannot be empty");
        }
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(label + " cannot be negative");
        }
        if (v.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(
                    label + " allows maximum 2 decimal places");
        }
        if (v.compareTo(MAX) > 0) {
            throw new BadRequestException(label + " is too large");
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private void requireStatus(AssetAudit d, String... allowed) {
        for (String a : allowed) {
            if (a.equals(d.getDisposalStatus())) {
                return;
            }
        }
        throw new BadRequestException(
                "Not allowed in status " + d.getDisposalStatus()
                        + " (allowed: " + String.join(", ", allowed) + ")");
    }
}