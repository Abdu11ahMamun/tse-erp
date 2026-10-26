package com.tse.erp.module.hr.service.impl;

import com.tse.erp.common.StatusUtil;
import com.tse.erp.exception.BadRequestException;
import com.tse.erp.exception.DuplicateResourceException;
import com.tse.erp.exception.ResourceNotFoundException;
import com.tse.erp.module.admin.entity.BusinessUnit;
import com.tse.erp.module.admin.repository.BusinessGroupRepository;
import com.tse.erp.module.admin.repository.BusinessUnitRepository;
import com.tse.erp.module.hr.entity.EmploymentStatus;
import com.tse.erp.module.hr.repository.EmployeeRepository;
import com.tse.erp.module.hr.repository.EmploymentStatusRepository;
import com.tse.erp.module.hr.service.EmploymentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmploymentStatusServiceImpl implements EmploymentStatusService {

    private final EmploymentStatusRepository employmentStatusRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessGroupRepository businessGroupRepository;
    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public List<EmploymentStatus> getAllEmploymentStatuses(Long buId) {
        if (buId == null) {
            return employmentStatusRepository
                    .findAllByOrderBySortOrderAscIdAsc();
        }
        return employmentStatusRepository
                .findByBuIdOrderBySortOrderAscIdAsc(buId);
    }

    @Override
    public EmploymentStatus getEmploymentStatusById(Long id) {
        return employmentStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment Status not found with id: " + id));
    }

    @Override
    public EmploymentStatus createEmploymentStatus(
            EmploymentStatus employmentStatus) {

        validateParents(employmentStatus.getBgId(),
                employmentStatus.getBuId());
        validateStatusName(employmentStatus.getStatusName());
        validateShortName(employmentStatus.getShortName());
        validateSortOrder(employmentStatus.getSortOrder());
        StatusUtil.validate(employmentStatus.getStatus());

        String name = employmentStatus.getStatusName().trim();
        String shortName = normalizeShortName(employmentStatus.getShortName());
        Long buId = employmentStatus.getBuId();

        // Duplicate check (BU-er moddhe, case-insensitive)
        if (!employmentStatusRepository
                .findByBuIdAndStatusNameIgnoreCase(buId, name).isEmpty()) {
            throw new DuplicateResourceException(
                    "Employment Status already exists: " + name);
        }
        if (shortName != null && !employmentStatusRepository
                .findByBuIdAndShortNameIgnoreCase(buId, shortName)
                .isEmpty()) {
            throw new DuplicateResourceException(
                    "Short name already exists: " + shortName);
        }

        employmentStatus.setId(null);
        employmentStatus.setStatusName(name);
        employmentStatus.setShortName(shortName);

        // Status default 1, BaseEntity @PrePersist handle korbe
        return employmentStatusRepository.save(employmentStatus);
    }

    @Override
    public EmploymentStatus updateEmploymentStatus(
            Long id, EmploymentStatus employmentStatus) {

        EmploymentStatus existing = getEmploymentStatusById(id);

        validateParents(employmentStatus.getBgId(),
                employmentStatus.getBuId());
        validateStatusName(employmentStatus.getStatusName());
        validateShortName(employmentStatus.getShortName());
        validateSortOrder(employmentStatus.getSortOrder());
        StatusUtil.validate(employmentStatus.getStatus());

        String name = employmentStatus.getStatusName().trim();
        String shortName = normalizeShortName(employmentStatus.getShortName());
        Long buId = employmentStatus.getBuId();

        // Duplicate check, nijer id bade
        boolean nameDuplicate = employmentStatusRepository
                .findByBuIdAndStatusNameIgnoreCase(buId, name).stream()
                .anyMatch(x -> !x.getId().equals(id));
        if (nameDuplicate) {
            throw new DuplicateResourceException(
                    "Employment Status already exists: " + name);
        }
        if (shortName != null) {
            boolean shortDuplicate = employmentStatusRepository
                    .findByBuIdAndShortNameIgnoreCase(buId, shortName)
                    .stream()
                    .anyMatch(x -> !x.getId().equals(id));
            if (shortDuplicate) {
                throw new DuplicateResourceException(
                        "Short name already exists: " + shortName);
            }
        }

        existing.setBgId(employmentStatus.getBgId());
        existing.setBuId(buId);
        existing.setStatusName(name);
        existing.setShortName(shortName);
        existing.setSortOrder(employmentStatus.getSortOrder());
        if (employmentStatus.getStatus() != null) {
            existing.setStatus(employmentStatus.getStatus());
        }

        return employmentStatusRepository.save(existing);
    }

    @Override
    public void deleteEmploymentStatus(Long id) {
        EmploymentStatus existing = getEmploymentStatusById(id);

        if (employeeRepository.existsByEmploymentStatusId(id)) {
            throw new BadRequestException(
                    "Cannot delete: Employees use this Employment Status");
        }

        employmentStatusRepository.delete(existing);
    }

    // =========================================
    // VALIDATION HELPERS
    // =========================================
    private void validateParents(Long bgId, Long buId) {
        if (bgId == null) {
            throw new BadRequestException(
                    "Business Group id cannot be empty");
        }
        if (buId == null) {
            throw new BadRequestException(
                    "Business Unit id cannot be empty");
        }

        businessGroupRepository.findById(bgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Group not found with id: " + bgId));

        BusinessUnit bu = businessUnitRepository.findById(buId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Business Unit not found with id: " + buId));

        if (!bgId.equals(bu.getBgId())) {
            throw new BadRequestException(
                    "Business Unit does not belong to selected Business Group");
        }
        if (!Integer.valueOf(1).equals(bu.getStatus())) {
            throw new BadRequestException(
                    "Selected Business Unit is not active");
        }
    }

    private void validateStatusName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Status name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new BadRequestException(
                    "Status name must be at least 2 characters");
        }
        if (name.trim().length() > 50) {
            throw new BadRequestException(
                    "Status name cannot exceed 50 characters");
        }
    }

    // Short name optional, blank hole skip
    private void validateShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return;
        }
        if (shortName.trim().length() < 2) {
            throw new BadRequestException(
                    "Short name must be at least 2 characters");
        }
        if (shortName.trim().length() > 5) {
            throw new BadRequestException(
                    "Short name cannot exceed 5 characters");
        }
    }

    private String normalizeShortName(String shortName) {
        if (shortName == null || shortName.trim().isEmpty()) {
            return null;
        }
        return shortName.trim();
    }

    // Sort order optional, > 0, fraction allowed (max 2 decimal)
    private void validateSortOrder(BigDecimal sortOrder) {
        if (sortOrder == null) {
            return;
        }
        if (sortOrder.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Sort order must be greater than 0");
        }
        if (sortOrder.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException(
                    "Sort order allows maximum 2 decimal places");
        }
        if (sortOrder.compareTo(new BigDecimal("99999999")) > 0) {
            throw new BadRequestException(
                    "Sort order cannot exceed 99999999");
        }
    }
}